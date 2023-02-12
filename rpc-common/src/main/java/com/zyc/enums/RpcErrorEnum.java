package com.zyc.enums;

/**
 * Rpc调用过程中的错误消息
 */
public enum RpcErrorEnum {
    SERVICE_NOT_FOUND("无法找到对应服务"),
    INVOKE_TIMEOUT("服务调用超时"),
    PRC_INVOKE_ERROR("调用过程出错"),
    NO_METHOD_MATCH("无匹配方法"),
    NOT_A_INTERFACE("非接口类型被传入"),
    NO_SERVICE_PROVIDED("未提供任何服务"),
    DUPLICATED_SERVICE_NAME("重复的服务名");

    final private String errorMsg;

    public String getErrorMsg() {
        return errorMsg;
    }

    RpcErrorEnum(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
