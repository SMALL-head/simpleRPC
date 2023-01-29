package com.zyc.entity;

import java.io.Serializable;

/**
 * Rpc请求参数
 */
public class RpcRequest implements Serializable {
    private String interfaceName;
    private String methodName;
    private Object[] param;
    private Class<?>[] paramClassType;

    public RpcRequest(String interfaceName, String methodName, Object[] param, Class<?>[] paramClassType) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.param = param;
        this.paramClassType = paramClassType;
    }

    // constructor, getters and setters
    public RpcRequest() {
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParam() {
        return param;
    }

    public void setParam(Object[] param) {
        this.param = param;
    }

    public Class<?>[] getParamClassType() {
        return paramClassType;
    }

    public void setParamClassType(Class<?>[] paramClassType) {
        this.paramClassType = paramClassType;
    }
}
