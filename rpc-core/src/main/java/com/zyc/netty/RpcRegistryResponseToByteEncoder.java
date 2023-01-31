package com.zyc.netty;

import com.zyc.entity.registry.RpcRegistryResponse;
import com.zyc.rpc.registry.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcRegistryResponseToByteEncoder extends MessageToByteEncoder<RpcRegistryResponse> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcRegistryResponse msg, ByteBuf out) throws Exception {
        byte[] bytes = Protocol.generateResponseProtocol(msg);
        out.writeBytes(bytes);
    }
}
