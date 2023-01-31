package com.zyc.entity.registry;

import com.zyc.enums.ResponseStatusEnum;

import java.io.Serializable;
import java.util.Map;

public class RpcRegistryResponse implements Serializable {
    public final static String SOCKET_ADDR_MAP_KEY = "SocketAddr";
    String msg;

    Map<String, Object> info;

    ResponseStatusEnum responseStatus;

    public ResponseStatusEnum getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(ResponseStatusEnum responseStatus) {
        this.responseStatus = responseStatus;
    }

    public RpcRegistryResponse() {
    }

    public RpcRegistryResponse(String msg, Map<String, Object> info, ResponseStatusEnum responseStatus) {
        this.msg = msg;
        this.info = info;
        this.responseStatus = responseStatus;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map<String, Object> getInfo() {
        return info;
    }

    public void setInfo(Map<String, Object> info) {
        this.info = info;
    }
}
