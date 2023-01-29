package com.zyc.entity;

import java.io.Serializable;

/**
 * 声明Rpc返回值消息
 * @param <T> 返回值类型
 */
public class RpcResponse<T> implements Serializable {
    private T response;

    private Class<?> responseType;

    // constructor, getters and setters
    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }

    public Class<?> getResponseType() {
        return responseType;
    }

    public void setResponseType(Class<?> responseType) {
        this.responseType = responseType;
    }
}
