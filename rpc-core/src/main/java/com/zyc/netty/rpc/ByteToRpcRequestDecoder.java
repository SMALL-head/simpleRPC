package com.zyc.netty.rpc;

import com.zyc.entity.rpc.RpcRequest;
import com.zyc.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ByteToRpcRequestDecoder extends ReplayingDecoder<Void> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            log.info("[ByteToRpcRequestDecoder]-开始反序列化");
            RpcRequest rpcRequest = Protocol.parseRpcRequestProtocol(in);
            log.info("[ByteToRpcRequestDecoder]-反序列化成功-{}", rpcRequest);
            out.add(rpcRequest);
        } catch (Exception e) {
            log.warn("[ByteToRpcRequestDecoder]-反序列化失败-交给下一个decoder解析");
            out.add(in);
        }
    }
}
