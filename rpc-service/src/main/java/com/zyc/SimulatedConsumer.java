package com.zyc;

import com.zyc.constants.Constants;
import com.zyc.entity.registry.RpcRegistryResponse;
import com.zyc.entity.rpc.GenericReturn;
import com.zyc.entity.rpc.RpcRequest;
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

/**
 * 消费者还没完成，用这个模拟消费者的请求
 */
@Slf4j
public class SimulatedConsumer {
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

        // 上面的内容都是为了连接消费者的（此处是模拟，因此并没有经过注册中心寻找信息，二是默认已经拿到信息去请求）

        // 调用服务测试
        RpcRequest request = new RpcRequest(
            MyService.class.getCanonicalName(), // 服务名字
            MyService.class.getMethods()[0].getName(), //服务的方法名
            new Object[]{1, 2}, // 参数
            new Class<?>[]{int.class, int.class});// 参数类型
        // 发送请求
        Channel channel = connect.channel();
        channel.writeAndFlush(request);
    }
}
