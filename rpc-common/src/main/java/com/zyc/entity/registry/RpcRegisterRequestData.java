package com.zyc.entity.registry;

import io.netty.util.internal.StringUtil;

import java.io.Serializable;
import java.util.Arrays;

public class RpcRegisterRequestData implements Serializable {
    private String host;
    private int port;
    String service;

    @Override
    public String toString() {
        return "RpcRegisterRequestData{" +
            "host='" + host + '\'' +
            ", port=" + port +
            ", service=" + service +
            '}';
    }

    //    Class<?>[] className;
//    Class<?> returnType;


    public boolean checkIfAnyFieldsNull() {
        return StringUtil.isNullOrEmpty(host) || port == 0 || service == null;
    }


    public RpcRegisterRequestData(String host, int port, String service) {
        this.host = host;
        this.port = port;
        this.service = service;
    }

    public RpcRegisterRequestData() {
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

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}
