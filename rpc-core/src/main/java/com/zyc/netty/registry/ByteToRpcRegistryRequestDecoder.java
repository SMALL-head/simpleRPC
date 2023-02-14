package com.zyc.netty.registry;

import com.zyc.entity.registry.HeartBeatData;
import com.zyc.entity.registry.RpcRegistryRequest;
import com.zyc.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ByteToRpcRegistryRequestDecoder extends ReplayingDecoder<Void> {
    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf byteBuf, List<Object> list) throws Exception {
        RpcRegistryRequest rpcRegisterRequest = null;
        HeartBeatData heartBeatData = null;
        byteBuf.markReaderIndex();

        try {
            rpcRegisterRequest = Protocol.parseRegistryRequestProtocol(byteBuf);
            list.add(rpcRegisterRequest);
            return;
        } catch (Exception ignored) {
        }

        byteBuf.resetReaderIndex();
        try {
            heartBeatData = Protocol.parseHeartBeatDataProtocol(byteBuf);
            list.add(heartBeatData);
        } catch (Exception ignored) {
        }
    }
}
