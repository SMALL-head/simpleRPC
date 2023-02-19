package com.zyc.rpc.server.handler;

import com.zyc.entity.rpc.GenericReturn;
import com.zyc.entity.rpc.RpcRequest;
import com.zyc.rpc.server.ServiceProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
@Slf4j
public class ServiceCallHandler extends ChannelInboundHandlerAdapter {
    Map<String, ServiceProvider<?>> serviceProviderMap;
    NioSocketChannel ch;
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

    public ServiceCallHandler(Map<String, ServiceProvider<?>> serviceProviderMap, NioSocketChannel ch) {
        this.serviceProviderMap = serviceProviderMap;
        this.ch = ch;
    }
}
