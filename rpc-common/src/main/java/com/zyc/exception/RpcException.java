package com.zyc.exception;

import com.zyc.enums.RpcErrorEnum;

public class RpcException extends RuntimeException{
    public RpcException(RpcErrorEnum errorEnum, String message) {
        super(errorEnum.getErrorMsg() + ":" + message);
    }
}
