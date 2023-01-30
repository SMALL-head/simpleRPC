package com.zyc.netty;

import com.zyc.entity.RpcRegisterRequest;
import com.zyc.rpc.registry.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcRegisterRequestToByteEncoder extends MessageToByteEncoder<RpcRegisterRequest> {
    @Override
    protected void encode(ChannelHandlerContext context, RpcRegisterRequest request, ByteBuf byteBuf) throws Exception {
        byte[] bytes = Protocol.generateProtocol(request);
        byteBuf.writeBytes(bytes);
    }
}
