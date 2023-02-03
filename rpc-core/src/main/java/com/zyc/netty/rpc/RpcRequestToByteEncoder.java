package com.zyc.netty.rpc;

import com.zyc.entity.rpc.RpcRequest;
import com.zyc.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcRequestToByteEncoder extends MessageToByteEncoder<RpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcRequest msg, ByteBuf out) throws Exception {
        log.info("[RpcRequestToByteEncoder]-进行序列化{}", msg.toString());
        byte[] serialize = Protocol.generateRpcRequestProtocol(msg);
        out.writeBytes(serialize);
    }
}
