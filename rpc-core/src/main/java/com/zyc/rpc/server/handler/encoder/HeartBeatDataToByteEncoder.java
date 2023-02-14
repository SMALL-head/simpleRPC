package com.zyc.rpc.server.handler.encoder;

import com.zyc.entity.registry.HeartBeatData;
import com.zyc.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class HeartBeatDataToByteEncoder extends MessageToByteEncoder<HeartBeatData> {
    @Override
    protected void encode(ChannelHandlerContext ctx, HeartBeatData msg, ByteBuf out) throws Exception {
        byte[] bytes = Protocol.generateHeartBeatDataProtocol(msg);
        out.writeBytes(bytes);
    }
}
