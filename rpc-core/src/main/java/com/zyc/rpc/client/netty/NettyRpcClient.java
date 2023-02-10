package com.zyc.rpc.client.netty;

import com.zyc.entity.registry.RpcRegisterRequestData;
import com.zyc.entity.registry.RpcRegistryRequest;
import com.zyc.entity.registry.RpcRegistryResponse;
import com.zyc.entity.registry.SocketInfo;
import com.zyc.entity.rpc.GenericReturn;
import com.zyc.entity.rpc.RpcRequest;
import com.zyc.enums.RpcErrorEnum;
import com.zyc.exception.RpcConnectionException;
import com.zyc.exception.RpcException;
import com.zyc.netty.registry.ByteToRpcRegistryResponseDecoder;
import com.zyc.netty.registry.RpcRegistryRequestToByteEncoder;
import com.zyc.netty.rpc.ByteToGenericReturnDecoder;
import com.zyc.netty.rpc.RpcRequestToByteEncoder;
import com.zyc.rpc.cache.InMemorySocketCache;
import com.zyc.rpc.cache.ServiceSocketCache;
import com.zyc.rpc.client.RpcClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class NettyRpcClient implements RpcClient {
    String registryHost;
    int registryPort;
    /**
     * 用来获取注册中心的连接Channel，注意到下面还有个rpcCallBootStrap也是用来获取channel的，
     * 由于注册中心的host和port是唯一的，因此这里直接记录channel
     * 而rpc连接的host和port不是唯一的（不同的服务可能来自不同的服务器），因此记录的是其他Bootstrap
     */
    ChannelFuture registryCenterConnect;

    /**
     * 用来记录服务器的线程组，实际上没什么用
     */
    private final NioEventLoopGroup nioEventLoopGroup;

    /**
     * 用来获取rpc调用时的请求Channel
     */
    Bootstrap rpcCallBootstrap;

    private final ConcurrentHashMap<String, CompletableFuture<RpcRegistryResponse>> registryResponseMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<GenericReturn>> rpcResponseMap = new ConcurrentHashMap<>();

    public NettyRpcClient(String registryHost, int registryPort) throws InterruptedException {
        this.registryHost = registryHost;
        this.registryPort = registryPort;

        // 用于连接注册中心的服务器
        Bootstrap bootstrap = new Bootstrap();
        nioEventLoopGroup = new NioEventLoopGroup();
        NioEventLoopGroup nioEventLoopGroupForRpcCall = new NioEventLoopGroup();
        registryCenterConnect = bootstrap
            .group(nioEventLoopGroup)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new ByteToRpcRegistryResponseDecoder())
                        .addLast(new RpcRegistryRequestToByteEncoder())
                        .addLast(new SimpleChannelInboundHandler<RpcRegistryResponse>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, RpcRegistryResponse msg) throws Exception {
                                log.debug("[NettyRpcClient]-[SimpleChannelInboundHandler]-收到来自注册中中心的消息-状态:{}", msg.getResponseStatus());
                                String msgID = msg.getMsgID();
                                CompletableFuture<RpcRegistryResponse> responseFuture = registryResponseMap.remove(msgID);
                                if (responseFuture == null) {
                                    log.error("[SimpleChannelInboundHandler]-[channelRead0]-未找到对应的{}对应的future", msgID);
                                    ctx.fireChannelRead(msg);
                                    return;
                                }
                                responseFuture.complete(msg);
                                log.debug("[SimpleChannelInboundHandler]-[channelRead0]-向msgID为{}的future写入结果", msgID);
                                ctx.fireChannelRead(msg);
                            }
                        });
                }
            })
            .connect(registryHost, registryPort);
        registryCenterConnect.sync();


        // 用于rpc远程调用的服务器
        Bootstrap bootstrapForRpcCall = new Bootstrap();
        rpcCallBootstrap = bootstrapForRpcCall
            .group(nioEventLoopGroupForRpcCall)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new ByteToGenericReturnDecoder())
                        .addLast(new RpcRequestToByteEncoder())
                        .addLast("RpcResultHandler", new SimpleChannelInboundHandler<GenericReturn>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, GenericReturn msg) throws Exception {
                                log.info("[NettyRpcClient]-[SimpleChannelInboundHandler]-收到rpc调用的结果-msgId = {}", msg.getMsgID());
                                String msgID = msg.getMsgID();
                                CompletableFuture<GenericReturn> future = rpcResponseMap.remove(msgID);
                                if (future == null) {
                                    log.error("[SimpleChannelInboundHandler]-[channelRead0]-未找到对应的{}对应的future", msgID);
                                    ctx.fireChannelRead(msg);
                                    return;
                                }
                                future.complete(msg);
                                log.debug("[SimpleChannelInboundHandler]-[channelRead0]-向msgID为{}的future写入结果", msgID);
                                ctx.fireChannelRead(msg);
                            }
                        });
                }
            });

    }

    @Override
    public CompletableFuture<RpcRegistryResponse> sendRegistryRequest(RpcRegistryRequest request) {

        CompletableFuture<RpcRegistryResponse> registryResponseFuture = new CompletableFuture<>();
        try {
            Channel channel = registryCenterConnect.channel();
            // 补充传输数据的socket信息
            InetSocketAddress socketAddress = (InetSocketAddress) channel.localAddress();
            RpcRegisterRequestData data = request.getData();
            data.setHost(socketAddress.getHostString());
            data.setPort(socketAddress.getPort());
            channel.writeAndFlush(request);

            log.debug("[NettyRpcClient]-[sendRegistryRequest]-向注册中心发送请求-TYPE={}-msgID={}", request.getType(), request.getMsgID());
            registryResponseMap.put(request.getMsgID(), registryResponseFuture);
            log.debug("[NettyPrcClient]-[sendRegistryRequest]-向ChanelMap中注册future,id={}", request.getMsgID());
        } catch (Exception e) {
            log.error("[NettyRpcClient]-[sendRegistryRequest]-发送RpcRegistryRequest失败");
            registryResponseMap.remove(request.getMsgID());
        }
        return registryResponseFuture;
    }

    @Override
    public CompletableFuture<GenericReturn> sendRpcRequest(RpcRequest request, SocketInfo serviceProviderSocketInfo) {
        Channel rpcCallChannel = null;
        String host = serviceProviderSocketInfo.getHost();
        int port = serviceProviderSocketInfo.getPort();
        try {
            rpcCallChannel = getRpcCallChannel(host, port);
        } catch (Exception e) {
            log.error("[sendRpcRequest]-未能连接到服务提供方, host: {}, port: {}", host, port);
            throw new RpcConnectionException("连接远程服务失败", host, port);
        }
        try {
            rpcCallChannel.writeAndFlush(request);
        } catch (Exception e) {
            log.error("[NettyRpcClient]-[sendRpcRequest]-rpc请求发出错误-msgID={}-请求服务类型={}", request.getMsgID(), request.getServiceName());
            throw new RpcException(RpcErrorEnum.PRC_INVOKE_ERROR, "rpc请求发出错误");
        }
        CompletableFuture<GenericReturn> future = new CompletableFuture<>();
        rpcResponseMap.put(request.getMsgID(), future);
        return future;
    }

    @Override
    public SocketInfo getSocketInfo() {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) registryCenterConnect.channel().localAddress();
        return new SocketInfo(inetSocketAddress.getHostString(), inetSocketAddress.getPort());
    }

    private Channel getRpcCallChannel(String serviceHost, int servicePort) throws InterruptedException {
        ChannelFuture connect = rpcCallBootstrap.connect(serviceHost, servicePort);
        // 别忘了阻塞！！要等连接建立后才执行后续的操作
        connect.sync();
        return connect.channel();
    }
}
