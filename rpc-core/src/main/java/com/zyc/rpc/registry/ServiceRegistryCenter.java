package com.zyc.rpc.registry;

import com.zyc.entity.registry.RpcRegistryRequest;
import com.zyc.entity.registry.SocketInfo;
import com.zyc.netty.ByteToRpcRegistryRequestDecoder;
import com.zyc.netty.RpcRegistryRequestToByteEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class ServiceRegistryCenter {
    final static String LOCALHOST = "127.0.0.1";
    private final ServiceRegistry serviceRegistry = new InMemoryServiceRegistryImpl();
    private String host;
    private int port;
    private NioEventLoopGroup workLoopGroup;

    public void serverStart() {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        this.workLoopGroup = eventLoopGroup;
        new ServerBootstrap().group(eventLoopGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel channel) throws Exception {
                channel.pipeline().addLast("RegisterRequestDecoder", new ByteToRpcRegistryRequestDecoder()).addLast("RegisterRequestEncoder", new RpcRegistryRequestToByteEncoder()).addLast(new StringDecoder()).addLast(new StringEncoder()).addLast("handler", new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        RpcRegistryRequest request = (RpcRegistryRequest) msg;
                        System.out.println(request);
                        // 根据协议类型进行不同的操作
                        switch (request.getType()) {
                            case REGISTRY_SERVICE -> {
                                // todo：注册服务
                                String service = request.getData().getService();
                                serviceRegistry.registry(service, request.getData().getHost(), request.getData().getPort());
                                channel.writeAndFlush("注册成功");
                            }
                            case GET_SERVICE -> {
                                //todo: 获取服务host和ip地址
                                String service = request.getData().getService();
                                SocketInfo serviceAddr = serviceRegistry.getServiceAddr(service);
                                channel.writeAndFlush(serviceAddr);
                            }
                            case OFFLINE_SERVICE -> {
                                // todo: 下线服务
                            }
                        }
                        ctx.fireChannelRead(msg);
                    }
                }).addLast(new ChannelOutboundHandlerAdapter() {
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                        super.write(ctx, msg, promise);
                    }
                });
            }
        }).bind(port);

//        new ServerBootstrap()
//            .group(new NioEventLoopGroup())
//            .channel(NioServerSocketChannel.class)
//            .childHandler(new ChannelInitializer<NioSocketChannel>() {
//                @Override
//                protected void initChannel(NioSocketChannel channel) throws Exception {
//                    channel.pipeline().addLast(new StringDecoder());
//                    channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
//                        @Override
//                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                            System.out.println(msg);
//                        }
//                    });
//                }
//
//            }).bind(8088);
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
