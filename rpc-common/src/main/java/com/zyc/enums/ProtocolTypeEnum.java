package com.zyc.enums;

public enum ProtocolTypeEnum {
    REGISTRY_SERVICE((byte)0x1, "注册服务"),
    GET_SERVICE((byte)0x2, "获取服务"),
    OFFLINE_SERVICE((byte)0x3, "服务下线");
    final byte value;
    final String desc;

    ProtocolTypeEnum(byte value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public byte getByteValue() {
        return value;
    }

    static public ProtocolTypeEnum getEnumByValue(byte value) {
        for (ProtocolTypeEnum e : ProtocolTypeEnum.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return null;
    }
}
