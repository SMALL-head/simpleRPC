package com.zyc.rpc.registry.config;

import com.zyc.entity.registry.SocketInfo;

public class RegistryConfig {
    static SocketInfo socketInfo;

    static public String getHost() {
        return socketInfo.getHost();
    }

    static public int getPort() {
        return socketInfo.getPort();
    }

    public static void setSocketInfo(SocketInfo socketInfo) {
        RegistryConfig.socketInfo = socketInfo;
    }
}
