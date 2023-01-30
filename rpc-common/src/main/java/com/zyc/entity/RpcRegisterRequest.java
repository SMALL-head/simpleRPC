package com.zyc.entity;

import java.io.Serializable;
import java.util.Arrays;

public class RpcRegisterRequest implements Serializable {
    private String host;
    private int port;
    String method;

    @Override
    public String toString() {
        return "RpcRegisterRequest{" +
            "host='" + host + '\'' +
            ", port=" + port +
            ", method='" + method + '\'' +
            ", className=" + Arrays.toString(className) +
            ", returnType=" + returnType +
            '}';
    }

    Class<?>[] className;
    Class<?> returnType;

    public void setMethod(String method) {
        this.method = method;
    }

    public Class<?>[] getClassName() {
        return className;
    }

    public void setClassName(Class<?>[] className) {
        this.className = className;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    public boolean checkIfAnyFieldsNull() {
        return host == null || port == 0 || method==null;
    }


    public RpcRegisterRequest(String host, int port, String method, Class<?>[] className, Class<?> returnType) {
        this.host = host;
        this.port = port;
        this.method = method;
        this.className = className;
        this.returnType = returnType;
    }

    public RpcRegisterRequest() {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getMethod() {
        return method;
    }

}
