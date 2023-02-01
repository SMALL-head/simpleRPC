package com.zyc.netty.registry;

import com.zyc.entity.registry.RpcRegistryResponse;
import com.zyc.rpc.registry.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class ByteToRpcRegistryResponseDecoder extends ReplayingDecoder<Void> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        try {
            RpcRegistryResponse response = Protocol.parseResponseProtocol(byteBuf);
            out.add(response);
        } catch (Exception ex) {
            out.add(byteBuf); // 产生异常后交给其他decoder处理
        }
    }
}
