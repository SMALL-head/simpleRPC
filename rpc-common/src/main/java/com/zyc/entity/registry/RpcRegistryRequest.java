package com.zyc.entity.registry;

import com.zyc.enums.ProtocolTypeEnum;
import com.zyc.utils.UUIDUtils;

import java.io.Serializable;
import java.util.UUID;

public class RpcRegistryRequest implements Serializable {
    private String msgID;

    private RpcRegisterRequestData data;
    private int version;

    ProtocolTypeEnum type;

    public RpcRegistryRequest(RpcRegisterRequestData data, int version, ProtocolTypeEnum type) {
        this.msgID = UUIDUtils.getUUiD();
        this.data = data;
        this.version = version;
        this.type = type;
    }

    public RpcRegisterRequestData getData() {
        return data;
    }

    public void setData(RpcRegisterRequestData data) {
        this.data = data;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public ProtocolTypeEnum getType() {
        return type;
    }

    public void setType(ProtocolTypeEnum type) {
        this.type = type;
    }

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }

    @Override
    public String toString() {
        return "RpcRegistryRequest{" +
            "data=" + data +
            ", version=" + version +
            ", type=" + type +
            '}';
    }
}
