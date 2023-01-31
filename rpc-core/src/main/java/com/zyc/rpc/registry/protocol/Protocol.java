package com.zyc.rpc.registry.protocol;

import com.zyc.constants.Constants;
import com.zyc.entity.registry.RpcRegisterRequestData;
import com.zyc.entity.registry.RpcRegistryRequest;
import com.zyc.entity.registry.RpcRegistryResponse;
import com.zyc.enums.ProtocolErrorEnum;
import com.zyc.enums.ProtocolTypeEnum;
import com.zyc.exception.ProtocolException;
import com.zyc.utils.ByteUtils;
import com.zyc.utils.Hessian2Utils;
import io.netty.buffer.ByteBuf;

/**
 * 协议结构
 * +--------------------------------------------------------------------------------------------------+
 * |   魔数(4B)  |   请求类型{@link ProtocolTypeEnum} (1B)   |   版本号(1B)   |   内容大小，单位为B(2B)   |
 * +--------------------------------------------------------------------------------------------------+
 * |                                                                                                  |
 * |                                               内容数据                                            |
 * |                                                                                                  |
 * +--------------------------------------------------------------------------------------------------+
 */
public class Protocol {
    /**
     * 通过rpcRegisterRequest，返回序列化后的byte数组
     * @param rpcRegisterRequestData rpcRegisterRequest,注册中心请求参数
     * @return 序列化后的结果
     * @throws Exception 序列化过程的错误
     */
    static public byte[] generateRequestProtocol(RpcRegisterRequestData rpcRegisterRequestData, ProtocolTypeEnum type) throws Exception {

        byte[] serialize = Hessian2Utils.serialize(rpcRegisterRequestData);

        byte[] res = new byte[serialize.length + 8];

        System.arraycopy(serialize, 0, res, 8, serialize.length);
        System.arraycopy(ByteUtils.int2byteArray(Constants.MAGIC_NUMBER), 0, res, 0, 4);
        System.arraycopy(new byte[]{type.getByteValue()}, 0, res, 4, 1);
        System.arraycopy(new byte[]{Constants.PROTOCOL_VERSION}, 0, res, 5, 1);

        if (serialize.length > Short.MAX_VALUE) {
            throw new ProtocolException(ProtocolErrorEnum.DATA_TOO_LONG);
        }
        short size = (short) (serialize.length);
        System.arraycopy(ByteUtils.short2byteArray(size), 0, res, 6, 2);
        return res;
    }

    static public RpcRegistryRequest parseRequestProtocol(ByteBuf content) throws Exception {
        int magic = content.readInt();
        if (magic != Constants.MAGIC_NUMBER) {
            throw new ProtocolException(ProtocolErrorEnum.MAGIC_NUMBER_ERROR);
        }
        byte b = content.readByte();
        ProtocolTypeEnum protocolType = ProtocolTypeEnum.getEnumByValue(b);
        int version = content.readByte();
        short size = content.readShort();

        byte[] serviceBytes = new byte[size];
        content.readBytes(serviceBytes);
        Object deserialize = Hessian2Utils.deserialize(serviceBytes);

        RpcRegisterRequestData data = (RpcRegisterRequestData) deserialize;

        return new RpcRegistryRequest(data, version, protocolType);
    }
    private static byte[] generateProtocolHead(byte[] res, int dataLength, ProtocolTypeEnum type) throws ProtocolException {
        System.arraycopy(ByteUtils.int2byteArray(Constants.MAGIC_NUMBER), 0, res, 0, 4);
        System.arraycopy(new byte[]{ProtocolTypeEnum.REGISTRY_RESPONSE.getByteValue()}, 0, res, 4, 1);
        System.arraycopy(new byte[]{Constants.PROTOCOL_VERSION}, 0, res, 5, 1);
        if (dataLength > Short.MAX_VALUE) {
            throw new ProtocolException(ProtocolErrorEnum.DATA_TOO_LONG);
        }
        short size = (short) (dataLength);
        System.arraycopy(ByteUtils.short2byteArray(size), 0, res, 6, 2);
        return res;
    }

    public static byte[] generateResponseProtocol(RpcRegistryResponse resp) throws Exception {
        byte[] serialize = Hessian2Utils.serialize(resp);

        byte[] res = new byte[serialize.length + 8];

        System.arraycopy(serialize, 0, res, 8, serialize.length);
        System.arraycopy(ByteUtils.int2byteArray(Constants.MAGIC_NUMBER), 0, res, 0, 4);
        System.arraycopy(new byte[]{ProtocolTypeEnum.REGISTRY_RESPONSE.getByteValue()}, 0, res, 4, 1);
        System.arraycopy(new byte[]{Constants.PROTOCOL_VERSION}, 0, res, 5, 1);

        if (serialize.length > Short.MAX_VALUE) {
            throw new ProtocolException(ProtocolErrorEnum.DATA_TOO_LONG);
        }
        short size = (short) (serialize.length);
        System.arraycopy(ByteUtils.short2byteArray(size), 0, res, 6, 2);
        return res;
    }
    public static RpcRegistryResponse parseResponseProtocol(ByteBuf content) throws Exception {
        int magic = content.readInt();
        if (magic != Constants.MAGIC_NUMBER) {
            throw new ProtocolException(ProtocolErrorEnum.MAGIC_NUMBER_ERROR);
        }
        byte b = content.readByte();
        ProtocolTypeEnum protocolType = ProtocolTypeEnum.getEnumByValue(b);
        int version = content.readByte(); // 跳过version
        short size = content.readShort();

        byte[] serviceBytes = new byte[size];
        content.readBytes(serviceBytes);

        return (RpcRegistryResponse) Hessian2Utils.deserialize(serviceBytes);
    }
}
