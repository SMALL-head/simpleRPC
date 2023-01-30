package com.zyc.netty;

import com.zyc.entity.registry.RpcRegistryRequest;
import com.zyc.rpc.registry.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcRegistryRequestToByteEncoder extends MessageToByteEncoder<RpcRegistryRequest> {
    @Override
    protected void encode(ChannelHandlerContext context, RpcRegistryRequest request, ByteBuf byteBuf) throws Exception {
        byte[] bytes = Protocol.generateProtocol(request.getData(), request.getType());
        byteBuf.writeBytes(bytes);
    }
}
