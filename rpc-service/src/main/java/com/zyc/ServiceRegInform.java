package com.zyc;

import com.zyc.constants.Constants;
import com.zyc.entity.registry.RpcRegisterRequestData;
import com.zyc.entity.registry.RpcRegistryRequest;
import com.zyc.entity.registry.RpcRegistryResponse;
import com.zyc.enums.ProtocolTypeEnum;
import com.zyc.netty.ByteToRpcRegistryResponseDecoder;
import com.zyc.netty.RpcRegistryRequestToByteEncoder;
import com.zyc.service.MyService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.concurrent.TimeUnit;

public class ServiceRegInform {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        ChannelFuture connect = new Bootstrap()
            .group(eventExecutors)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline
                        .addLast(new RpcRegistryRequestToByteEncoder())
                        .addLast("decoder", new ByteToRpcRegistryResponseDecoder())
                        .addLast(new StringDecoder())
                        .addLast(new StringEncoder())
                        .addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                if (msg instanceof RpcRegistryResponse resp) {
                                    System.out.println("msg = " + resp.getMsg());
                                }
                                ctx.fireChannelRead(msg); // 传递责任链
                            }
                        });
                }
            })
            .connect(Constants.LOCALHOST, 8088);

        connect.sync();
        RpcRegisterRequestData data = new RpcRegisterRequestData(Constants.LOCALHOST, 8080, MyService.class.getCanonicalName());
        RpcRegistryRequest request = new RpcRegistryRequest(data, Constants.PROTOCOL_VERSION, ProtocolTypeEnum.REGISTRY_SERVICE);
        Channel channel = connect.channel();
        channel.writeAndFlush(request);

        TimeUnit.SECONDS.sleep(7);

        RpcRegisterRequestData requestForService = new RpcRegisterRequestData(Constants.LOCALHOST, 8080, MyService.class.getCanonicalName());
        RpcRegistryRequest request2 = new RpcRegistryRequest(data, Constants.PROTOCOL_VERSION, ProtocolTypeEnum.GET_SERVICE);
        channel.writeAndFlush(request2);
    }
}
