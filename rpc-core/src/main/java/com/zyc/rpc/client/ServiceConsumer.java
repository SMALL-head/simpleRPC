package com.zyc.rpc.client;

import com.zyc.constants.Constants;
import com.zyc.entity.registry.*;
import com.zyc.entity.registry.collections.ServiceInfoSet;
import com.zyc.entity.rpc.GenericReturn;
import com.zyc.entity.rpc.RpcRequest;
import com.zyc.enums.ProtocolTypeEnum;
import com.zyc.enums.ResponseStatusEnum;
import com.zyc.enums.RpcErrorEnum;
import com.zyc.exception.RpcConnectionException;
import com.zyc.exception.RpcException;
import com.zyc.rpc.cache.InMemorySocketCache;
import com.zyc.rpc.cache.ServiceSocketCache;
import com.zyc.rpc.client.netty.NettyRpcClient;
import com.zyc.rpc.loadbalence.LoadBalanceStrategy;
import com.zyc.rpc.loadbalence.LoadBalancer;
import com.zyc.rpc.registry.ServiceRegistry;
import com.zyc.rpc.registry.config.RegistryConfig;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.TypeDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class ServiceConsumer<T> {
    /**
     * service对外不可见，serviceProxy对外可见
     */
    final private T serviceProxy;

    String serviceName;

    /**
     * 用来进行服务查找和rpc调用的客户端
     */
    RpcClient client;

    boolean cacheEnable = true;

    LoadBalancer loadBalancer = new LoadBalancer();

    /**
     * 服务注册中心返回信息的cache
     */
    ServiceSocketCache serviceSocketCache = InMemorySocketCache.getInstance();

    @SuppressWarnings("unchecked")
    public ServiceConsumer(Class<?> serviceInterface) throws InterruptedException {
        if (!serviceInterface.isInterface()) {
            throw new RpcException(RpcErrorEnum.NOT_A_INTERFACE, "需要一个接口类型");
        }
        this.serviceProxy = (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 1. 寻找服务提供者的位置，首先在cache里面寻找
                ServiceInfoSet serviceAddrs= findServiceAddr();
                if (serviceAddrs.isEmpty()) {
                    log.warn("[ServiceConsumer]-[proxy]-服务{}的缓存列表为空，直接向注册中心请求", serviceName);
                    serviceAddrs = findServiceAddr(serviceName, false);
                }
                if (serviceAddrs.isEmpty()) {
                    log.warn("[ServiceConsumer]-[proxy]-服务{}注册中心请求结果仍然为空", serviceName);
                    return null;
                }
                log.info("[ServiceConsumer]-[proxy]-获取到服务{}地址信息", serviceName);

                // 2. 整理方法调用的参数
                Class<?>[] classTypes = method.getParameterTypes();

                // 3. 封装rpc调用参数，并发送
                // todo: 负载均衡代码应有修改
                RpcRequest request = new RpcRequest(serviceName, method.getName(), args, classTypes);
                CompletableFuture<GenericReturn> genericReturnCompletableFuture;
                // 3.1 装载备选项
                loadBalancer.setServiceInfoSet(serviceAddrs);
                // 3.2 触发负载均衡选择
                ServiceInfo serviceAddr = loadBalancer.select();
                SocketInfo socketInfo = serviceAddr.getSocketInfo();
                try {
                    genericReturnCompletableFuture = client.sendRpcRequest(request, new SocketInfo(socketInfo.getHost(), socketInfo.getPort()));
                } catch (RpcConnectionException rpcConnectionException) {
                    serviceAddrs = findServiceAddr(serviceName, false);
                    // 3.1 装载备选项
                    loadBalancer.setServiceInfoSet(serviceAddrs);
                    // 3.2 触发负载均衡选择
                    serviceAddr = loadBalancer.select();
                    socketInfo = serviceAddr.getSocketInfo();
                    genericReturnCompletableFuture = client.sendRpcRequest(request, new SocketInfo(socketInfo.getHost(), socketInfo.getPort()));
                }
                log.debug("[ServiceConsumer]-[proxy]-发送rpc请求-msgID={}-serviceName={}-serviceMethod={}", request.getMsgID(), request.getServiceName(), request.getServiceMethod());

                // 4.同步阻塞得到结果
                GenericReturn genericReturn = null;
                try {
                    genericReturn = genericReturnCompletableFuture.get(2, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (TimeoutException e) {
                    throw new RpcException(RpcErrorEnum.INVOKE_TIMEOUT, "响应超时");
                }
                return genericReturn.getValue();
            }
        });

        this.client = new NettyRpcClient(RegistryConfig.getHost(), RegistryConfig.getPort());
        this.serviceName = serviceInterface.getCanonicalName();
    }

    public ServiceConsumer(Class<?> serviceInterface, String serviceName) throws InterruptedException {
        this(serviceInterface);
        if (!StringUtil.isNullOrEmpty(serviceName)) {
            this.serviceName = serviceName;
        }
    }

    private ServiceInfoSet findServiceAddr() throws Exception {
        return findServiceAddr(serviceName, cacheEnable);
    }

    private ServiceInfoSet findServiceAddr(String serviceName, boolean cacheEnable) throws Exception {
        if (!cacheEnable) {
            ServiceInfoSet serviceAddr = findServiceAddr(serviceName); // 此处如果没找到服务会抛出异常，就不会进行缓存了
            serviceSocketCache.cache(serviceName, serviceAddr);
            return serviceAddr;
        }
        ServiceInfoSet socketInfo = serviceSocketCache.find(serviceName);
        if (socketInfo != null) {
            return socketInfo;
        }
        ServiceInfoSet serviceAddr = findServiceAddr(serviceName);
        serviceSocketCache.cache(serviceName, serviceAddr);
        return serviceAddr;
    }

    private ServiceInfoSet findServiceAddr(String serviceName) throws Exception {
        // 1. 封装并发送
        SocketInfo socketInfo = client.getSocketInfo();
        log.debug("[findServiceAddr]-获取到消费者的socket信息,host: {}, port: {}", socketInfo.getHost(), socketInfo.getPort());
        RpcRegisterRequestData data = new RpcRegisterRequestData(socketInfo.getHost(), socketInfo.getPort(), serviceName);
        RpcRegistryRequest request = new RpcRegistryRequest(data, Constants.PROTOCOL_VERSION, ProtocolTypeEnum.GET_SERVICE);
        CompletableFuture<RpcRegistryResponse> future = client.sendRegistryRequest(request);

        // 等待响应结果，并返回响应结果
        RpcRegistryResponse rpcRegistryResponse = null;
        try {
            // 同步阻塞的调用，并且设置超时时间2s
            rpcRegistryResponse = future.get(2, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RpcException(RpcErrorEnum.INVOKE_TIMEOUT, "响应超时");
        }
        if (rpcRegistryResponse.getResponseStatus() == ResponseStatusEnum.FAIL_GET_SERVICE) {
            log.error("[ServiceConsumer]-[findServiceAddr]-无法获取服务:{}",
                rpcRegistryResponse.getMsg());
            throw new RpcException(RpcErrorEnum.SERVICE_NOT_FOUND, "未能获取服务名为" + serviceName + "的服务");
        }
        Object o = rpcRegistryResponse.getInfo().get(RpcRegistryResponse.SOCKET_ADDR_MAP_KEY);
        if (o == null) {
            log.warn("[ServiceConsumer]-[findServiceAddr]-响应消息不全，缺失项为{}", RpcRegistryResponse.SOCKET_ADDR_MAP_KEY);
            throw new RpcException(RpcErrorEnum.SERVICE_NOT_FOUND, "响应消息有缺失");
        }
        if (!(o instanceof ServiceInfoSet)) {
            log.error("[ServiceConsumer]-[findServiceAddr]-响应消息类型错误, {}, {}", o.getClass(), o);
            throw new RpcException(RpcErrorEnum.SERVICE_NOT_FOUND, "响应信息有误");
        }
        return (ServiceInfoSet) o;

    }

    public T getServiceProxy() {
        return serviceProxy;
    }

    public boolean isCacheEnable() {
        return cacheEnable;
    }

    public void setCacheEnable(boolean cacheEnable) {
        this.cacheEnable = cacheEnable;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public void setLoadBalancerStrategy(LoadBalanceStrategy strategy) {
        loadBalancer.setStrategy(strategy);
    }
}
