package com.zyc.entity.rpc;

import lombok.ToString;

import java.io.Serializable;

@ToString
public class RpcRequest implements Serializable {
    String serviceName;
    String serviceMethod;
    Object[] params;
    Class<?>[] paramsType;

    public RpcRequest(String serviceName, String serviceMethod, Object[] params, Class<?>[] paramsTypes) {
        this.serviceName = serviceName;
        this.serviceMethod = serviceMethod;
        this.params = params;
        this.paramsType = paramsTypes;
    }

    public RpcRequest() {
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceMethod() {
        return serviceMethod;
    }

    public void setServiceMethod(String serviceMethod) {
        this.serviceMethod = serviceMethod;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public Class<?>[] getParamsType() {
        return paramsType;
    }

    public void setParamsType(Class<?>[] paramsType) {
        this.paramsType = paramsType;
    }
}
