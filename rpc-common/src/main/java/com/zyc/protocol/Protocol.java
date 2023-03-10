package com.zyc.protocol;

import com.zyc.constants.Constants;
import com.zyc.entity.registry.HeartBeatData;
import com.zyc.entity.registry.RpcRegisterRequestData;
import com.zyc.entity.registry.RpcRegistryRequest;
import com.zyc.entity.registry.RpcRegistryResponse;
import com.zyc.entity.rpc.GenericReturn;
import com.zyc.entity.rpc.RpcRequest;
import com.zyc.enums.ProtocolErrorEnum;
import com.zyc.enums.ProtocolTypeEnum;
import com.zyc.exception.ProtocolException;
import com.zyc.utils.ByteUtils;
import com.zyc.utils.Hessian2Utils;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class Protocol {
    /**
     * 通过rpcRegisterRequest，返回序列化后的byte数组
     *
     * @param rpcRegisterRequest rpcRegisterRequest,注册中心请求参数
     * @return 序列化后的结果
     * @throws Exception 序列化过程的错误
     */
    static public byte[] generateRegistryRequestProtocol(RpcRegistryRequest rpcRegisterRequest, ProtocolTypeEnum type) throws Exception {

        byte[] serialize = Hessian2Utils.serialize(rpcRegisterRequest);

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

    static public RpcRegistryRequest parseRegistryRequestProtocol(ByteBuf content) throws Exception {
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

        RpcRegistryRequest data = (RpcRegistryRequest) deserialize;

        return data;
    }

    /**
     * 生成协议的首部信息
     * @param res 最终生成的协议的byte数组
     * @param dataLength 仅数据长度，不包含首部长度
     * @param type 类型
     * @throws ProtocolException 协议产生过程中的可能出现的错误
     */
    private static void generateProtocolHead(byte[] res, int dataLength, ProtocolTypeEnum type) throws ProtocolException {
        System.arraycopy(ByteUtils.int2byteArray(Constants.MAGIC_NUMBER), 0, res, 0, 4);
        System.arraycopy(new byte[]{type.getByteValue()}, 0, res, 4, 1);
        System.arraycopy(new byte[]{Constants.PROTOCOL_VERSION}, 0, res, 5, 1);
        if (dataLength > Short.MAX_VALUE) {
            throw new ProtocolException(ProtocolErrorEnum.DATA_TOO_LONG);
        }
        short size = (short) (dataLength);
        System.arraycopy(ByteUtils.short2byteArray(size), 0, res, 6, 2);
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

    public static byte[] generateRpcRequestProtocol(RpcRequest request) throws Exception {
        byte[] content = Hessian2Utils.serialize(request);
        byte[] res = new byte[content.length + 8]; // 数据长度

        // 生成协议头部(8B)
        generateProtocolHead(res, content.length, ProtocolTypeEnum.RPC_CALL);

        // 生成data部分
        System.arraycopy(content, 0, res, 8, content.length);
        return res;
    }

    public static RpcRequest parseRpcRequestProtocol(ByteBuf content) throws Exception {
        //校验魔数
        int magic = content.readInt();
        if (magic != Constants.MAGIC_NUMBER) {
            log.warn("[parseRpcRequestProtocol]-魔数错误-{}", magic);
            throw new ProtocolException(ProtocolErrorEnum.MAGIC_NUMBER_ERROR);
        }

        // 校验协议类型
        byte b = content.readByte();
        ProtocolTypeEnum protocolType = ProtocolTypeEnum.getEnumByValue(b);
        if (!ProtocolTypeEnum.RPC_CALL.equals(protocolType)) {
            log.warn("[parseRpcRequestProtocol]-协议类型错误-预期RPC_CALL-实际{}", protocolType);
            throw new ProtocolException(ProtocolErrorEnum.UNEXPECTED_PROTOCOL_TYPE);
        }
        // 跳过version
        int version = content.readByte();
        // 获取size后反序列化
        short size = content.readShort();
        byte[] serviceBytes = new byte[size];
        content.readBytes(serviceBytes);
        return (RpcRequest) Hessian2Utils.deserialize(serviceBytes);
    }

    public static byte[] generateGenericReturnProtocol(GenericReturn resp) throws Exception {
        byte[] content = Hessian2Utils.serialize(resp);
        byte[] res = new byte[content.length + 8];

        generateProtocolHead(res, content.length, ProtocolTypeEnum.RPC_RESPONSE);
        // 生成data部分
        System.arraycopy(content, 0, res, 8, content.length);
        return res;
    }

    public static GenericReturn parseGenericReturnProtocol(ByteBuf content) throws Exception {
        //校验魔数
        int magic = content.readInt();
        if (magic != Constants.MAGIC_NUMBER) {
            throw new ProtocolException(ProtocolErrorEnum.MAGIC_NUMBER_ERROR);
        }
        // 校验协议类型
        byte b = content.readByte();
        ProtocolTypeEnum protocolType = ProtocolTypeEnum.getEnumByValue(b);
        if (!ProtocolTypeEnum.RPC_RESPONSE.equals(protocolType)) {
            throw new ProtocolException(ProtocolErrorEnum.UNEXPECTED_PROTOCOL_TYPE);
        }
        // 跳过version
        int version = content.readByte();
        // 获取size后反序列化
        short size = content.readShort();
        byte[] serviceBytes = new byte[size];
        content.readBytes(serviceBytes);
        return (GenericReturn) Hessian2Utils.deserialize(serviceBytes);
    }

    public static byte[] generateHeartBeatDataProtocol(HeartBeatData data) throws Exception {
        byte[] serialize = Hessian2Utils.serialize(data);
        byte[] res = new byte[serialize.length + 8];
        generateProtocolHead(res, serialize.length, ProtocolTypeEnum.HEART_BEAT);
        System.arraycopy(serialize, 0, res, 8, serialize.length);
        return res;
    }

    public static HeartBeatData parseHeartBeatDataProtocol(ByteBuf content) throws Exception {
        //校验魔数
        int magic = content.readInt();
        if (magic != Constants.MAGIC_NUMBER) {
            throw new ProtocolException(ProtocolErrorEnum.MAGIC_NUMBER_ERROR);
        }
        // 校验协议类型
        byte b = content.readByte();
        ProtocolTypeEnum protocolType = ProtocolTypeEnum.getEnumByValue(b);
        if (!ProtocolTypeEnum.HEART_BEAT.equals(protocolType)) {
            throw new ProtocolException(ProtocolErrorEnum.UNEXPECTED_PROTOCOL_TYPE);
        }
        // 跳过version
        int version = content.readByte();
        // 获取size后反序列化
        short size = content.readShort();
        byte[] serviceBytes = new byte[size];
        content.readBytes(serviceBytes);
        return (HeartBeatData) Hessian2Utils.deserialize(serviceBytes);
    }
}
