package com.zyc.rpc.server;

import ch.qos.logback.core.hook.ShutdownHook;
import com.zyc.annotations.ServiceReference;
import com.zyc.annotations.ServiceScan;
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
import com.zyc.netty.registry.ByteToRpcRegistryResponseDecoder;
import com.zyc.netty.registry.RpcRegistryRequestToByteEncoder;
import com.zyc.netty.rpc.ByteToRpcRequestDecoder;
import com.zyc.netty.rpc.GenericReturnToByteEncoder;
import com.zyc.rpc.registry.config.RegistryConfig;
import com.zyc.utils.ClassUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.Future;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcServer {
    Map<String, ServiceProvider<?>> serviceProviderMap;

    String host;
    int port;

    NioEventLoopGroupForShutdown parentEventLoop;

    class NioEventLoopGroupForShutdown extends NioEventLoopGroup {
        @Override
        public Future<?> shutdownGracefully() {
            // 做好offline操作
            log.info("即将关闭java进程");
            serviceOffline();
            return super.shutdownGracefully();
        }
    }

    ChannelFuture registerCenterConnectFuture;

    public void startServer(Class<?> bootClass) throws Exception {
        if (serviceProviderMap == null || serviceProviderMap.isEmpty()) {
            log.error("[startServer]-未检测到服务");
            throw new RpcException(RpcErrorEnum.NO_SERVICE_PROVIDED, "需要注册至少一个服务");
        }

        scanServices(bootClass);
        parentEventLoop = new NioEventLoopGroupForShutdown();
        new ServerBootstrap()
            .channel(NioServerSocketChannel.class)
            .group(parentEventLoop)
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new ByteToRpcRequestDecoder())
                        .addLast(new GenericReturnToByteEncoder())
                        .addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info("[RpcServer]-[startServer]-接收到msg，类型为-{}", msg.getClass());
                                if (!(msg instanceof RpcRequest request)) {
                                    return;
                                }

                                String serviceName = request.getServiceName();
                                ServiceProvider<?> serviceProvider = serviceProviderMap.get(serviceName);
                                if (serviceProvider == null) {
                                    log.warn("未寻找到服务{}", serviceName);
                                    ctx.fireChannelRead(msg);
                                    return;
                                }
                                GenericReturn genericReturn;
                                try {

                                    genericReturn = serviceProvider.callService(request);
                                } catch (Exception e) {
                                    ctx.fireChannelRead(msg);
                                    throw e;
                                }
                                log.info("成功调用服务{}", serviceName);
                                ch.writeAndFlush(genericReturn);
                                ctx.fireChannelRead(msg);
                            }
                        });
                }
            })
            .bind(port);
        log.info("[RpcServer]-[startServer]-rpc服务提供方服务器注册host={}-port={}", host, port);

        registerCenterConnectFuture = new Bootstrap()
            .channel(NioSocketChannel.class)
            .group(new NioEventLoopGroup())
            .handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new RpcRegistryRequestToByteEncoder())
                        .addLast(new ByteToRpcRegistryResponseDecoder())
                        .addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                if (!(msg instanceof RpcRegistryResponse response)) {
                                    ctx.fireChannelRead(msg);
                                    return;
                                }
                                if (ResponseStatusEnum.SUCCESS_REGISTRY.equals(response.getResponseStatus())) {
                                    log.info("[RpcServer]-[handler]-成功注册服务-服务名{}", response.getInfo().get("serviceName"));
                                }
                                ctx.fireChannelRead(msg);
                            }
                        });
                }
            })
            .connect(RegistryConfig.getHost(), RegistryConfig.getPort());
        registerCenterConnectFuture.sync();
        Channel channel = registerCenterConnectFuture.channel();
        sendRegistryRequest(channel);

        // 注册jvm退出的hook函数
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("关闭进程");
            shutdown();
        }));
    }

    /**
     * 扫描指定包下所有带有ServiceReference注解的类，将其注册进入serviceProviderMap中
     */
    private void scanServices(Class<?> bootClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        String packageName = bootClass.getPackageName();
        ServiceScan annotation = bootClass.getAnnotation(ServiceScan.class);
        if (annotation != null) {
            packageName = annotation.scan();
        }
        log.info("[scanServices]-扫描包{}下的类", packageName);

        // 尝试获取packageName下的注解类
        Set<Class<?>> classesWithAnnotation = ClassUtil.getClassesWithAnnotation(packageName, ServiceReference.class);
        log.info("[scanServices]-扫描完成，共有{}个满足条件的类", classesWithAnnotation.size());
        for (Class<?> classWithAnnotation : classesWithAnnotation) {
            log.info("[scanService]-第一个类{}", classWithAnnotation.getCanonicalName());
            // 暂时仅默认构造函数构造方法
            Constructor<?> defaultConstructor = classWithAnnotation.getConstructor();
            Object o = defaultConstructor.newInstance();
            String serviceName = classWithAnnotation.getAnnotation(ServiceReference.class).value();
            if (StringUtil.isNullOrEmpty(serviceName)) {
                serviceName = o.getClass().getCanonicalName();
            }
            ServiceProvider serviceProvider = new ServiceProvider(o, serviceName);
            ServiceProvider<?> origin = serviceProviderMap.putIfAbsent(serviceName, serviceProvider);
            if (origin != null) {
                log.error("[scanServices]-检测到相同名字的serviceProvider，重复名字为{}", serviceName);
                throw new RpcException(RpcErrorEnum.DUPLICATED_SERVICE_NAME, "");
            }
        }
    }

    private void sendRegistryRequest(Channel channel) {
        for (String serviceName : serviceProviderMap.keySet()) {
            RpcRegisterRequestData data = new RpcRegisterRequestData(host, port, serviceName);
            RpcRegistryRequest request = new RpcRegistryRequest(data, Constants.PROTOCOL_VERSION, ProtocolTypeEnum.REGISTRY_SERVICE);
            channel.writeAndFlush(request);
        }
    }

    public boolean addService(ServiceProvider<?> serviceProvider) {
        if (serviceProviderMap == null) {
            throw new RuntimeException("serviceProviderMap为null");
        }
        serviceProviderMap.put(serviceProvider.getServiceName(), serviceProvider);
        return true;
    }

    private void serviceOffline() {
        log.info("关闭RpcServiceServer");
        Channel channel = registerCenterConnectFuture.channel();
        for (String serviceName : serviceProviderMap.keySet()) {
            RpcRegisterRequestData data = new RpcRegisterRequestData(host, port, serviceName);
            RpcRegistryRequest offlineRequest = new RpcRegistryRequest(data, Constants.PROTOCOL_VERSION, ProtocolTypeEnum.OFFLINE_SERVICE);
            log.debug("[RpcServer]-[serviceOffline]-下线服务{}", serviceName);
            channel.writeAndFlush(offlineRequest);
        }
    }

    private void serviceOffline(String serviceName) {
        Channel channel = registerCenterConnectFuture.channel();
        RpcRegisterRequestData data = new RpcRegisterRequestData(host, port, serviceName);
        RpcRegistryRequest offlineRequest = new RpcRegistryRequest(data, Constants.PROTOCOL_VERSION, ProtocolTypeEnum.OFFLINE_SERVICE);
        channel.writeAndFlush(offlineRequest);
    }

    public void shutdown() {
        parentEventLoop.shutdownGracefully();
    }

    public RpcServer(Map<String, ServiceProvider<?>> serviceProviderMap) {
        this.serviceProviderMap = serviceProviderMap;
    }

    public RpcServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.serviceProviderMap = new ConcurrentHashMap<>();
    }

    public Map<String, ServiceProvider<?>> getServiceProviderMap() {
        return serviceProviderMap;
    }

    public void setServiceProviderMap(Map<String, ServiceProvider<?>> serviceProviderMap) {
        this.serviceProviderMap = serviceProviderMap;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
