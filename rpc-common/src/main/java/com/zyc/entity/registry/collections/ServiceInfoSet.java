package com.zyc.entity.registry.collections;

import com.zyc.entity.registry.ServiceInfo;

import java.util.HashSet;

public class ServiceInfoSet extends HashSet<ServiceInfo> {
    String serviceName;

    public ServiceInfoSet() {
    }

    public ServiceInfoSet(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
