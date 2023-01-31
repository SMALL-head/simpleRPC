package com.zyc.enums;

public enum ResponseStatusEnum {
    SUCCESS_REGISTRY("注册服务成功"),
    SUCCESS_GET_SERVICE("成功获取服务"),
    FAIL_GET_SERVICE("未获取到服务"),
    SUCCESS_OFFLINE_SERVICE("成功下线服务");

    final String desc;

    ResponseStatusEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
