package com.zyc.rpc.server;

import com.zyc.annotations.ServiceReference;
import com.zyc.annotations.ServiceScan;
import com.zyc.constants.Constants;
import com.zyc.entity.registry.*;
import com.zyc.enums.ProtocolTypeEnum;
import com.zyc.enums.ResponseStatusEnum;
import com.zyc.enums.RpcErrorEnum;
import com.zyc.exception.RpcException;
import com.zyc.netty.registry.ByteToRpcRegistryResponseDecoder;
import com.zyc.netty.registry.RpcRegistryRequestToByteEncoder;
import com.zyc.netty.rpc.ByteToRpcRequestDecoder;
import com.zyc.netty.rpc.GenericReturnToByteEncoder;
import com.zyc.rpc.registry.config.RegistryConfig;
import com.zyc.rpc.server.handler.ServiceCallHandler;
import com.zyc.rpc.server.handler.encoder.HeartBeatDataToByteEncoder;
import com.zyc.utils.ClassUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcServer {
    final static int heartBeatSendInterval = 10;
    /**
     * 索引层
     */
    Map<String, ServiceProvider<?>> serviceProviderMap;
    String host;
    int port;

    NioEventLoopGroupForShutdown parentEventLoop;

    public void offlineService(String service1) {
        serviceOffline(service1);
    }

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

        // 启动rpc调用相关的server
        new ServerBootstrap()
            .channel(NioServerSocketChannel.class)
            .group(parentEventLoop)
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new ByteToRpcRequestDecoder())
                        .addLast(new GenericReturnToByteEncoder())
                        .addLast("RpcCallHandler", new ServiceCallHandler(serviceProviderMap, ch));
                }
            })
            .bind(port);
        log.info("[RpcServer]-[startServer]-rpc服务提供方服务器注册host={}-port={}", host, port);

        // 启动和注册中心相连的客户端
        registerCenterConnectFuture = new Bootstrap()
            .channel(NioSocketChannel.class)
            .group(new NioEventLoopGroup())
            .handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new RpcRegistryRequestToByteEncoder())
                        .addLast(new ByteToRpcRegistryResponseDecoder())
                        .addLast(new HeartBeatDataToByteEncoder())
                        .addLast("msgFromRegistryHandler", new ChannelInboundHandlerAdapter() {
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
        // 注：此处channel会一直带到心跳包的发送，因此不会被GC，长连接
        Channel channel = registerCenterConnectFuture.channel();

        // 发送所有serviceProvider的注册信息
        sendRegistryRequest(channel);

        // 保持channel的连接状态，进行心跳包的发送
        sendHeartBeatMsgToRegistryCenter(channel);

        // 注册jvm退出的hook函数
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("关闭进程");
            shutdown();
        }));
    }

    private void sendHeartBeatMsgToRegistryCenter(Channel channel) {
        // 指定间隔后发送一次心跳包，定时任务调度
        // 不是很大的花销，因此核心线程数给到1就好
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            log.info("[sendHeartBeatMsgToRegistryCenter]-正在发送server中所有存活服务的心跳包");
            for (String serviceName : serviceProviderMap.keySet()) {
                channel.writeAndFlush(new HeartBeatData(serviceName));
            }
            log.info("[sendHeartBeatMsgToRegistryCenter]-成功发送所有心跳包");
        }, 6/*等待所有服务启动后再进行发送*/, heartBeatSendInterval, TimeUnit.SECONDS);
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
        serviceProviderMap.clear();
    }

    private void serviceOffline(String serviceName) {
        serviceProviderMap.remove(serviceName);
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
