package com.zyc.netty;

import com.zyc.entity.registry.RpcRegisterRequestData;
import com.zyc.rpc.registry.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcRegisterRequestToByteEncoder extends MessageToByteEncoder<RpcRegisterRequestData> {
    @Override
    protected void encode(ChannelHandlerContext context, RpcRegisterRequestData request, ByteBuf byteBuf) throws Exception {
        byte[] bytes = Protocol.generateProtocol(request);
        byteBuf.writeBytes(bytes);
    }
}
