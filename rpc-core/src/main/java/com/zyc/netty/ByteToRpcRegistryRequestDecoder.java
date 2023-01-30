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
        RpcRegistryRequest rpcRegisterRequestData = Protocol.parseProtocol(byteBuf);
        list.add(rpcRegisterRequestData);
    }
}
