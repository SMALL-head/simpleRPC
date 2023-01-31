package com.zyc.rpc.registry;

import com.zyc.entity.registry.RpcRegisterRequestData;
import com.zyc.entity.registry.RpcRegistryRequest;
import com.zyc.entity.registry.RpcRegistryResponse;
import com.zyc.entity.registry.SocketInfo;
import com.zyc.enums.ResponseStatusEnum;
import com.zyc.netty.ByteToRpcRegistryRequestDecoder;
import com.zyc.netty.RpcRegistryRequestToByteEncoder;
import com.zyc.netty.RpcRegistryResponseToByteEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
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
                channel.pipeline()
                    .addLast("RegisterRequestDecoder", new ByteToRpcRegistryRequestDecoder())
                    .addLast("RegisterResponseEncoder", new RpcRegistryResponseToByteEncoder())
                    .addLast(new StringDecoder()).
                    addLast(new StringEncoder())
                    .addLast("RegisterCenterRequestHandler", new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        if (msg instanceof RpcRegistryRequest request) {
                            // 根据协议类型进行不同的操作
                            final RpcRegisterRequestData data = request.getData();
                            switch (request.getType()) {
                                case REGISTRY_SERVICE -> {
                                    String service = data.getService();
                                    serviceRegistry.registry(service, data.getHost(), data.getPort());
                                    log.info("服务：{} - 注册成功", service);
                                    RpcRegistryResponse resp = new RpcRegistryResponse(ResponseStatusEnum.SUCCESS_REGISTRY.getDesc(), null, ResponseStatusEnum.SUCCESS_REGISTRY);
                                    channel.writeAndFlush(resp);
                                }
                                case GET_SERVICE -> {
                                    //注册中心获取服务后向客户端返回服务所在的socket地址
                                    String service = data.getService();
                                    log.info("来自 {} 的服务查询请求 - {}", data.getHost() + ":" + data.getPort(), service);
                                    SocketInfo serviceAddr = serviceRegistry.getServiceAddr(service);
                                    log.info("[注册中心查询服务] - {} - {}", service, serviceAddr);
                                    ResponseStatusEnum status = serviceAddr == null ? ResponseStatusEnum.FAIL_GET_SERVICE : ResponseStatusEnum.SUCCESS_GET_SERVICE;
                                    Map<String, Object> info = new HashMap<>();
                                    if (serviceAddr != null) {
                                        info.put(RpcRegistryResponse.SOCKET_ADDR_MAP_KEY, serviceAddr);
                                    }
                                    RpcRegistryResponse resp = new RpcRegistryResponse(status.getDesc(), info, status);
                                    channel.writeAndFlush(resp);
                                    log.info("向客户端{}发送服务地址信息", data.getHost() + ":" + data.getPort());
                                }
                                case OFFLINE_SERVICE -> {
                                    // todo: 下线服务
                                }
                            }
                        } else {
                            log.warn("[RegisterCenterRequestHandler]-[channelRead]-" +
                                    "接收到非RpcRegistryRequest的参数-类型为{}", msg.getClass());
                        }
                        ctx.fireChannelRead(msg);
                    }
                });
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
