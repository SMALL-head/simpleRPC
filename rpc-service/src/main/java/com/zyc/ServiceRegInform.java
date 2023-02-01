package com.zyc;

import com.zyc.constants.Constants;
import com.zyc.entity.registry.RpcRegisterRequestData;
import com.zyc.entity.registry.RpcRegistryRequest;
import com.zyc.entity.registry.RpcRegistryResponse;
import com.zyc.entity.rpc.GenericReturn;
import com.zyc.entity.rpc.RpcRequest;
import com.zyc.enums.ProtocolTypeEnum;
import com.zyc.netty.registry.ByteToRpcRegistryResponseDecoder;
import com.zyc.netty.registry.RpcRegistryRequestToByteEncoder;
import com.zyc.netty.rpc.ByteToGenericReturnDecoder;
import com.zyc.netty.rpc.RpcRequestToByteEncoder;
import com.zyc.service.MyService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
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
                        .addLast(new RpcRequestToByteEncoder())
                        .addLast(new ByteToGenericReturnDecoder())
                        .addLast(new StringDecoder())
                        .addLast(new StringEncoder())
                        .addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                if (msg instanceof RpcRegistryResponse resp) {
                                    System.out.println("msg = " + resp.getMsg());
                                } else if (msg instanceof GenericReturn returnValue) {
                                    log.info("得到返回结果{}", returnValue.getValue());
                                }
                                ctx.fireChannelRead(msg); // 传递责任链
                            }
                        });
                }
            })
            .connect(Constants.LOCALHOST, 8080);

        connect.sync();

        // 调用服务测试
        RpcRequest request = new RpcRequest(MyService.class.getCanonicalName(), MyService.class.getMethods()[0].getName(), new Object[]{1, 2}, new Class<?>[]{int.class, int.class});
        Channel channel = connect.channel();
        channel.writeAndFlush(request);
    }
}
