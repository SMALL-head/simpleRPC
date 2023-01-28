package com.zyc.entity;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;

public class RpcResponse<T> implements Serializable {
    private T response;

    private Class<?> responseType;

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
