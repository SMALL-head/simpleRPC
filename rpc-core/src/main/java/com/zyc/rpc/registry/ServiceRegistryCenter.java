package com.zyc.rpc.registry;

import com.zyc.constants.Constants;
import com.zyc.netty.registry.ByteToRpcRegistryRequestDecoder;
import com.zyc.netty.registry.RpcRegistryResponseToByteEncoder;
import com.zyc.rpc.registry.handler.HeartBeatReceiveHandler;
import com.zyc.rpc.registry.handler.RegisterCenterRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ServiceRegistryCenter {
    final static String LOCALHOST = "127.0.0.1";
    private final ServiceRegistry serviceRegistry = new InMemoryServiceRegistryImpl();
    private String host;
    private int port;
    private NioEventLoopGroup workLoopGroup;

    /**
     * 定时任务：用于删除失效服务的
     */
    private ScheduledExecutorService scheduledExecutorService;

    public void serverStart() {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        this.workLoopGroup = eventLoopGroup;
        new ServerBootstrap()
            .group(eventLoopGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel channel) throws Exception {
                    channel.pipeline()
                        .addLast("RegisterRequestDecoder", new ByteToRpcRegistryRequestDecoder())
                        .addLast("RegisterResponseEncoder", new RpcRegistryResponseToByteEncoder())
                        .addLast("RegisterCenterRequestHandler", new RegisterCenterRequestHandler(serviceRegistry, channel))
                        .addLast("heartBeatHandler", new HeartBeatReceiveHandler(serviceRegistry));
                }
                {
                    log.info("注册中心启动，host:{}, port:{}", host, port);
                }
            }).bind(port);

        // 启动定时任务检查serviceRegistry
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService
            .scheduleAtFixedRate(serviceRegistry::removeDeadService, 6, Constants.REGISTRY_CHECK_INTERVAL, TimeUnit.SECONDS);
    }

    public void shutdown() {
        this.workLoopGroup.shutdownGracefully();
        this.scheduledExecutorService.shutdown();
    }

    public ServiceRegistryCenter(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public ServiceRegistryCenter(int port) {
        this.host = LOCALHOST;
        this.port = port;
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
