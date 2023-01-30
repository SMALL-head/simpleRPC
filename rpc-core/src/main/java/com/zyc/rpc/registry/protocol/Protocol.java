package com.zyc.rpc.registry.protocol;

import com.zyc.constants.Constants;
import com.zyc.entity.RpcRegisterRequest;
import com.zyc.enums.ProtocolTypeEnum;
import com.zyc.utils.ByteUtils;
import com.zyc.utils.Hessian2Utils;
import io.netty.buffer.ByteBuf;

/**
 * 协议结构
 * +----------------------------------------------------------------------------------+
 * | 魔数(4B)| 请求类型{@link ProtocolTypeEnum} (1B) | 版本号(1B) | 内容大小，单位为B(2B) |
 * +----------------------------------------------------------------------------------+
 * |                                                                                  |
 * |                               内容数据                                            |
 * |                                                                                  |
 * +----------------------------------------------------------------------------------+
 */
public class Protocol {
    /**
     * 通过rpcRegisterRequest，返回序列化后的byte数组
     * @param rpcRegisterRequest rpcRegisterRequest,注册中心请求参数
     * @return 序列化后的结果
     * @throws Exception 序列化过程的错误
     */
    static public byte[] generateProtocol(RpcRegisterRequest rpcRegisterRequest) throws Exception {

        byte[] serialize = Hessian2Utils.serialize(rpcRegisterRequest);

        byte[] res = new byte[serialize.length + 8];

        System.arraycopy(serialize, 0, res, 8, serialize.length);
        System.arraycopy(ByteUtils.int2byteArray(Constants.MAGIC_NUMBER), 0, res, 0, 4);
        System.arraycopy(new byte[]{ProtocolTypeEnum.REGISTRY_SERVICE.getByteValue()}, 0, res, 4, 1);
        System.arraycopy(new byte[]{Constants.PROTOCOL_VERSION}, 0, res, 5, 1);
        short size = (short) (serialize.length);
        System.arraycopy(ByteUtils.short2byteArray(size), 0, res, 6, 2);
        return res;
    }

    static public RpcRegisterRequest parseProtocol(ByteBuf content) throws Exception {
        int magic = content.readInt();
        byte b = content.readByte();
        ProtocolTypeEnum protocolType = ProtocolTypeEnum.getEnumByValue(b);
        int version = content.readByte();
        int size = content.readShort();



        byte[] serviceBytes = new byte[size];
        content.readBytes(serviceBytes);
        Object deserialize = Hessian2Utils.deserialize(serviceBytes);
        return (RpcRegisterRequest) deserialize;
    }
}
