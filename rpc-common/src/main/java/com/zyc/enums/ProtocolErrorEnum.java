package com.zyc.enums;

public enum ProtocolErrorEnum {
    DATA_TOO_LONG("数据内容过多"),
    MAGIC_NUMBER_ERROR("魔数不正确"),
    DATA_ITEM_FAULT("数据项缺失"),
    UNEXPECTED_PROTOCOL_TYPE("非期望类型的协议类型");

    final String desc;

    ProtocolErrorEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
