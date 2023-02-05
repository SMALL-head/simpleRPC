package com.zyc.utils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyUtils {
    public static ChannelFuture generateConnection(String host, int port, ChannelHandler... handlers) {
        return new Bootstrap()
            .group(new NioEventLoopGroup())
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    for (ChannelHandler handler : handlers) {
                        pipeline.addLast(handler);
                    }
                }
            })
            .connect(host, port);
    }
}
