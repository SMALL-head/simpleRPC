package com.zyc.entity.registry;

import com.zyc.enums.ProtocolTypeEnum;

public class RpcRegistryRequest {
    private RpcRegisterRequestData data;

    @Override
    public String toString() {
        return "RpcRegistryRequest{" +
            "data=" + data +
            ", version=" + version +
            ", type=" + type +
            '}';
    }

    private int version;

    ProtocolTypeEnum type;

    public RpcRegistryRequest(RpcRegisterRequestData data, int version, ProtocolTypeEnum type) {
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
}
