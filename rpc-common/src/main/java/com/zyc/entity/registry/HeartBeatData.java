package com.zyc.entity.registry;

import java.io.Serializable;

/**
 * 心跳包
 */
public class HeartBeatData implements Serializable {
    String serviceName;

    String host;

    int port;

    public HeartBeatData() {
    }

//    public HeartBeatData(String serviceName) {
//        this.serviceName = serviceName;
//    }

    public HeartBeatData(String serviceName, String host, int port) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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
}
