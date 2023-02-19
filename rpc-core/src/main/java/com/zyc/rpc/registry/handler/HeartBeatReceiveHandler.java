package com.zyc.rpc.registry.handler;

import com.zyc.entity.registry.HeartBeatData;
import com.zyc.rpc.registry.ServiceRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartBeatReceiveHandler extends SimpleChannelInboundHandler<HeartBeatData> {
    private final ServiceRegistry serviceRegistry;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HeartBeatData msg) throws Exception {
        String serviceName = msg.getServiceName();
        String host = msg.getHost();
        int port = msg.getPort();
        log.info("[HeartBeatReceiveHandler]-接收到服务{}的心跳包，并进行更新", serviceName);
        serviceRegistry.updateLastUpdate(serviceName, host, port);
    }

    public HeartBeatReceiveHandler(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
}
