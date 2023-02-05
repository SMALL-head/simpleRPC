package com.zyc.rpc.client;

import com.zyc.constants.Constants;
import com.zyc.entity.registry.RpcRegisterRequestData;
import com.zyc.entity.registry.RpcRegistryRequest;
import com.zyc.entity.registry.RpcRegistryResponse;
import com.zyc.entity.registry.SocketInfo;
import com.zyc.entity.rpc.GenericReturn;
import com.zyc.entity.rpc.RpcRequest;
import com.zyc.enums.ProtocolTypeEnum;
import com.zyc.enums.ResponseStatusEnum;
import com.zyc.enums.RpcErrorEnum;
import com.zyc.exception.RpcException;
import com.zyc.rpc.client.netty.NettyRpcClient;
import com.zyc.rpc.registry.config.RegistryConfig;
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

    final private Class<?> serviceInterface;

    /**
     * 用来进行服务查找和rpc调用的客户端
     */
    RpcClient client;

    @SuppressWarnings("unchecked")
    public ServiceConsumer(Class<?> serviceInterface) throws InterruptedException {
        this.serviceProxy = (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 1. 寻找服务提供者的位置 todo:后期可以加入缓存机制
                SocketInfo serviceAddr = findServiceAddr();
                log.info("[ServiceConsumer]-[proxy]-获取到服务的socket-host={}, port={}", serviceAddr.getHost(), serviceAddr.getPort());

                // 2. 整理方法调用的参数
                Class<?>[] classTypes = method.getParameterTypes();

                // 3. 封装rpc调用参数，并发送
                RpcRequest request = new RpcRequest(serviceInterface.getCanonicalName(), method.getName(), args, classTypes);
                CompletableFuture<GenericReturn> genericReturnCompletableFuture = client.sendRpcRequest(request, new SocketInfo(serviceAddr.getHost(), serviceAddr.getPort()));
                log.info("[ServiceConsumer]-[proxy]-发送rpc请求-msgID={}-serviceName={}-serviceMethod={}", request.getMsgID(), request.getServiceName(), request.getServiceMethod());

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
        if (!serviceInterface.isInterface()) {
            throw new RpcException(RpcErrorEnum.NOT_A_INTERFACE, "需要一个接口类型");
        }
        this.serviceInterface = serviceInterface;
    }

    public SocketInfo findServiceAddr() throws InterruptedException {
        return findServiceAddr(serviceInterface.getCanonicalName());
    }

    private SocketInfo findServiceAddr(String serviceName) throws InterruptedException {
        // 1. 封装并发送
        RpcRegisterRequestData data = new RpcRegisterRequestData(null, 0, serviceName);
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
        if (rpcRegistryResponse.getResponseStatus() != ResponseStatusEnum.SUCCESS_GET_SERVICE) {
            log.warn("[ServiceConsumer]-[findServiceAddr]-响应类型有误，本应该为SUCCESS_GET_SERVICE，实际获得{}",
                rpcRegistryResponse.getResponseStatus());
            throw new RpcException(RpcErrorEnum.SERVICE_NOT_FOUND, "[ServiceConsumer]-[findServiceAddr]-响应类型有误");
        }
        Object o = rpcRegistryResponse.getInfo().get(RpcRegistryResponse.SOCKET_ADDR_MAP_KEY);
        if (o == null) {
            log.warn("[ServiceConsumer]-[findServiceAddr]-响应消息不全，缺失项为{}", RpcRegistryResponse.SOCKET_ADDR_MAP_KEY);
            throw new RpcException(RpcErrorEnum.SERVICE_NOT_FOUND, "响应消息有缺失");
        }
        if (!(o instanceof SocketInfo)) {
            log.error("[ServiceConsumer]-[findServiceAddr]-响应消息类型错误");
            throw new RpcException(RpcErrorEnum.SERVICE_NOT_FOUND, "响应信息有误");
        }
        return (SocketInfo) o;

    }

    public T getServiceProxy() {
        return serviceProxy;
    }
}
