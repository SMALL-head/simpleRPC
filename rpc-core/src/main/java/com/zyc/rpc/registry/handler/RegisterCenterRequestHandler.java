package com.zyc.rpc.registry.handler;

import com.zyc.entity.registry.RpcRegisterRequestData;
import com.zyc.entity.registry.RpcRegistryRequest;
import com.zyc.entity.registry.RpcRegistryResponse;
import com.zyc.entity.registry.SocketInfo;
import com.zyc.enums.ResponseStatusEnum;
import com.zyc.rpc.registry.ServiceRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RegisterCenterRequestHandler extends SimpleChannelInboundHandler<RpcRegistryRequest> {
    private final ServiceRegistry serviceRegistry;
    NioSocketChannel channel;

    public RegisterCenterRequestHandler(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public RegisterCenterRequestHandler(ServiceRegistry serviceRegistry, NioSocketChannel channel) {
        this.serviceRegistry = serviceRegistry;
        this.channel = channel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRegistryRequest request) throws Exception {
        String msgId = request.getMsgID();
        log.info("[ServiceRegistryCenter]-[ChannelInboundHandlerAdapter]-注册中心获取消息，msgID={}", msgId);
        // 根据协议类型进行不同的操作
        final RpcRegisterRequestData data = request.getData();
        switch (request.getType()) {
            case REGISTRY_SERVICE -> {
                String service = data.getService();
                serviceRegistry.registry(service, data.getHost(), data.getPort());
                log.info("服务：{} - 注册成功", service);
                Map<String, Object> info = new HashMap<>();
                info.put("serviceName", service);
                RpcRegistryResponse resp = new RpcRegistryResponse(msgId, ResponseStatusEnum.SUCCESS_REGISTRY.getDesc(), info, ResponseStatusEnum.SUCCESS_REGISTRY);
                channel.writeAndFlush(resp);
            }
            case GET_SERVICE -> {
                //注册中心获取服务后向客户端返回服务所在的socket地址
                String service = data.getService();
                log.info("[ServiceRegistryCenter]-[ChannelInboundHandlerAdapter]-来自 {} 的服务查询请求 - {}", data.getHost() + ":" + data.getPort(), service);
                SocketInfo serviceAddr = serviceRegistry.getServiceAddr(service);
                log.info("[ServiceRegistryCenter]-[ChannelInboundHandlerAdapter]注册中心查询服务-服务名：{} -结果：{}", service, serviceAddr);
                ResponseStatusEnum status = serviceAddr == null ? ResponseStatusEnum.FAIL_GET_SERVICE : ResponseStatusEnum.SUCCESS_GET_SERVICE;
                Map<String, Object> info = new HashMap<>();
                String respMsg = null;
                if (serviceAddr != null) {
                    info.put(RpcRegistryResponse.SOCKET_ADDR_MAP_KEY, serviceAddr);
                    respMsg = "成功获取地址" + service;
                    // 记录消费者及其对应的服务，当服务下线的时候需要向消费者发送cache清除的消息
                } else {
                    respMsg = "未能获取到名为" + service + "的服务";
                }
                RpcRegistryResponse resp = new RpcRegistryResponse(msgId, respMsg, info, status);
                channel.writeAndFlush(resp);
                log.info("向客户端{}发送服务地址信息", data.getHost() + ":" + data.getPort());
            }
            case OFFLINE_SERVICE -> {
                String service = data.getService();
                log.debug("[ServiceRegistryCenter]-[handler]-收到下线请求，下线服务{}", service);
                boolean b = serviceRegistry.offlineService(service);
                RpcRegistryResponse resp = new RpcRegistryResponse(msgId, service + ResponseStatusEnum.SUCCESS_OFFLINE_SERVICE.getDesc(), null, ResponseStatusEnum.SUCCESS_OFFLINE_SERVICE);

                // 可能遭遇另一端channel下线的问题，因此尝试捕获Exception
                try {
                    channel.writeAndFlush(resp);
                } catch (Throwable e) {
                    ctx.fireChannelRead(request);
                }
            }
        }
    }
}

