package com.zyc.entity.registry;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;

public class ServiceInfo implements Serializable {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInfo that = (ServiceInfo) o;
        return socketInfo.equals(that.socketInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(socketInfo);
    }

    @Override
    public String toString() {
        return "ServiceInfo{" +
            "socketInfo=" + socketInfo;
    }
}
