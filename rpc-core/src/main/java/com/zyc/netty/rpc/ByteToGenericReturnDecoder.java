package com.zyc.netty.rpc;

import com.zyc.entity.rpc.GenericReturn;
import com.zyc.rpc.registry.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class ByteToGenericReturnDecoder extends ReplayingDecoder<Void> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            GenericReturn genericReturn = Protocol.parseGenericReturnProtocol(in);
            out.add(genericReturn);
        } catch (Exception e) {
            out.add(in);
        }
    }
}
