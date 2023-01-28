package com.zyc.enums;

public enum RpcErrorEnum {
    SERVICE_NOT_FOUND("无法找到对应服务"),
    INVOKE_TIMEOUT("服务调用超时");

    final private String errorMsg;

    public String getErrorMsg() {
        return errorMsg;
    }

    RpcErrorEnum(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
