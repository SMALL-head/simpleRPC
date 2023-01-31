package com.zyc.entity.registry;

import java.io.Serializable;

public class SocketInfo implements Serializable {
    private String host;
    private int port;

    public SocketInfo(String host, int port) {
        this.host = host;
        this.port = port;
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

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
