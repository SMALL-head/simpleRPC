package com.zyc.netty.rpc;

import com.zyc.entity.rpc.GenericReturn;
import com.zyc.rpc.registry.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class GenericReturnToByteEncoder extends MessageToByteEncoder<GenericReturn> {
    @Override
    protected void encode(ChannelHandlerContext ctx, GenericReturn msg, ByteBuf out) throws Exception {
        byte[] bytes = Protocol.generateGenericReturnProtocol(msg);
        out.writeBytes(bytes);
    }
}
