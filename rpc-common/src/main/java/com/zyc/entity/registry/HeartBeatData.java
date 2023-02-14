package com.zyc.entity.registry;

import java.io.Serializable;

/**
 * 心跳包
 */
public class HeartBeatData implements Serializable {
    String serviceName;

    public HeartBeatData() {
    }

    public HeartBeatData(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
