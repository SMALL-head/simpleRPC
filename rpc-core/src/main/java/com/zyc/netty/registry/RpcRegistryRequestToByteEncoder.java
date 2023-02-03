package com.zyc.netty.registry;

import com.zyc.entity.registry.RpcRegistryRequest;
import com.zyc.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcRegistryRequestToByteEncoder extends MessageToByteEncoder<RpcRegistryRequest> {
    @Override
    protected void encode(ChannelHandlerContext context, RpcRegistryRequest request, ByteBuf byteBuf) throws Exception {
        byte[] bytes = Protocol.generateRegistryRequestProtocol(request.getData(), request.getType());
        byteBuf.writeBytes(bytes);
    }
}
