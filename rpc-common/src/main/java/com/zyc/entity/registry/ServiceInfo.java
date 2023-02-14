package com.zyc.entity.registry;

import java.util.Calendar;

public class ServiceInfo {
    SocketInfo socketInfo;

    Calendar lastUpdate;

    public ServiceInfo(SocketInfo socketInfo, Calendar lastUpdate) {
        this.socketInfo = socketInfo;
        this.lastUpdate = lastUpdate;
    }

    public ServiceInfo() {
    }

    public SocketInfo getSocketInfo() {
        return socketInfo;
    }

    public void setSocketInfo(SocketInfo socketInfo) {
        this.socketInfo = socketInfo;
    }

    public Calendar getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Calendar lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
