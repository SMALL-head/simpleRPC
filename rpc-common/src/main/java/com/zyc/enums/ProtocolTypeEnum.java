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

    /**
     * 根据value变量获取枚举变量，比如传入1可以得到"注册服务"的REGISTRY_SERVICE。
     * 返回值可能为空，因此必要时候需要进行nullptr校验
     * @param value value值，比如
     * @return 对应的枚举变量；若没有对应的则返回null
     */
    static public ProtocolTypeEnum getEnumByValue(byte value) {
        // 数量比较少， 直接搜就行
        for (ProtocolTypeEnum e : ProtocolTypeEnum.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return null;
    }
}
