package com.zyc.rpc.loadbalence;

import com.zyc.entity.registry.ServiceInfo;
import com.zyc.entity.registry.collections.ServiceInfoSet;

public interface LoadBalanceStrategy {
    ServiceInfo select(ServiceInfoSet serviceInfoSet);
}
