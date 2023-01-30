package com.zyc.rpc.registry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ServiceRegistryCenter {
    final static String LOCALHOST = "127.0.0.1";
    private final ServiceRegistry serviceRegistry = new ServiceRegistryRepositoryImpl();
    private String host;
    private int port;
    private NioEventLoopGroup workLoopGroup;

    public void serverStart() {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        this.workLoopGroup = eventLoopGroup;
        new ServerBootstrap()
            .group(eventLoopGroup)
            .channel(NioSctpServerChannel.class)
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel channel) throws Exception {
//                    channel.pipeline().addLast("");
                }
            }).bind(port);
    }

    public void shutdown() {
        this.workLoopGroup.shutdownGracefully();
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
