package com.zyc.netty;

import com.zyc.entity.registry.RpcRegistryRequest;
import com.zyc.rpc.registry.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class ByteToRpcRegistryRequestDecoder extends ReplayingDecoder<Void> {
    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf byteBuf, List<Object> list) throws Exception {
        try {
            RpcRegistryRequest rpcRegisterRequest = Protocol.parseRequestProtocol(byteBuf);
            list.add(rpcRegisterRequest);
        } catch (Exception ex) {
            list.add(byteBuf); // 产生异常后交给其他decoder处理
        }
    }
}
