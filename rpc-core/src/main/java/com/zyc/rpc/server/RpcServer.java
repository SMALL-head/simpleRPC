package com.zyc.rpc.server;

import com.zyc.constants.Constants;
import com.zyc.entity.registry.RpcRegisterRequestData;
import com.zyc.entity.registry.RpcRegistryRequest;
import com.zyc.entity.registry.RpcRegistryResponse;
import com.zyc.entity.registry.SocketInfo;
import com.zyc.entity.rpc.GenericReturn;
import com.zyc.entity.rpc.RpcRequest;
import com.zyc.enums.ProtocolTypeEnum;
import com.zyc.enums.ResponseStatusEnum;
import com.zyc.netty.registry.ByteToRpcRegistryResponseDecoder;
import com.zyc.netty.registry.RpcRegistryRequestToByteEncoder;
import com.zyc.netty.rpc.ByteToRpcRequestDecoder;
import com.zyc.netty.rpc.GenericReturnToByteEncoder;
import com.zyc.rpc.registry.config.RegistryConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class RpcServer {
    Map<String, ServiceProvider<?>> serviceProviderMap;

    String host;
    int port;

    NioEventLoopGroup parentEventLoop;

    public void startServer() throws Exception {
        parentEventLoop = new NioEventLoopGroup();
        new ServerBootstrap()
            .channel(NioServerSocketChannel.class)
            .group(parentEventLoop)
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new ByteToRpcRequestDecoder())
                        .addLast(new GenericReturnToByteEncoder())
                        .addLast(new ChannelInboundHandlerAdapter(){
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

        ChannelFuture connect = new Bootstrap()
            .channel(NioSocketChannel.class)
            .group(new NioEventLoopGroup())
            .handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new RpcRegistryRequestToByteEncoder()).addLast(new ByteToRpcRegistryResponseDecoder())
                        .addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                if (!(msg instanceof RpcRegistryResponse response)) {
                                    ctx.fireChannelRead(msg);
                                    return;
                                }
                                if (ResponseStatusEnum.SUCCESS_REGISTRY.equals(response.getResponseStatus())) {
                                    log.info("[RpcServer]-成功注册服务-{}", response.getMsg());
                                } else {
                                    log.error("[RpcServer]-注册服务失败");
                                }
                                ctx.fireChannelRead(msg);
                            }
                        });
                }
            })
            .connect(RegistryConfig.getHost(), RegistryConfig.getPort());
        connect.sync();
        Channel channel = connect.channel();
        sendRegistryRequest(channel);
    }

    private void sendRegistryRequest(Channel channel) {
        for (String serviceName : serviceProviderMap.keySet()) {
            RpcRegisterRequestData data = new RpcRegisterRequestData(host, port, serviceName);
            RpcRegistryRequest request = new RpcRegistryRequest(data, Constants.PROTOCOL_VERSION, ProtocolTypeEnum.REGISTRY_SERVICE);
            channel.writeAndFlush(request);
        }
    }

    public RpcServer(Map<String, ServiceProvider<?>> serviceProviderMap) {
        this.serviceProviderMap = serviceProviderMap;
    }

    public RpcServer(String host, int port) {
        this.host = host;
        this.port = port;
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
