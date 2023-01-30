package rpc.client;

import com.zyc.constants.Constants;
import com.zyc.entity.registry.RpcRegisterRequestData;
import com.zyc.entity.registry.RpcRegistryRequest;
import com.zyc.enums.ProtocolTypeEnum;
import com.zyc.netty.ByteToRpcRegistryRequestDecoder;
import com.zyc.netty.RpcRegistryRequestToByteEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.junit.Test;
import rpc.serviceDemo.ServiceDemoImpl;

import java.net.InetSocketAddress;

public class SimServiceRegisterTest {
    @Test
    public void reg() throws InterruptedException {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        ChannelFuture connect = new Bootstrap()
            .group(eventExecutors)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new ByteToRpcRegistryRequestDecoder())
                        .addLast(new RpcRegistryRequestToByteEncoder())
                        .addLast(new StringDecoder())
                        .addLast(new StringEncoder())
                        .addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                System.out.println("msg = " + msg);
                                ctx.fireChannelRead(msg);
                            }
                        });
                }
            })
            .connect(new InetSocketAddress(Constants.LOCALHOST, 8088));
        connect.sync(); // 等待连接成立
        RpcRegisterRequestData data = new RpcRegisterRequestData(Constants.LOCALHOST, 8080, ServiceDemoImpl.class.getCanonicalName());
        RpcRegistryRequest request = new RpcRegistryRequest(data, Constants.PROTOCOL_VERSION, ProtocolTypeEnum.REGISTRY_SERVICE);
        connect.channel().writeAndFlush(request);

        Thread.currentThread().join();

    }
}
