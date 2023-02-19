package com.zyc.rpc.loadbalence.impl;

import com.zyc.entity.registry.ServiceInfo;
import com.zyc.entity.registry.collections.ServiceInfoSet;
import com.zyc.rpc.loadbalence.LoadBalanceStrategy;

import java.util.Random;

public class RandomLoadBalanceStrategy implements LoadBalanceStrategy {
    Random random = new Random(System.currentTimeMillis());
    @Override
    public ServiceInfo select(ServiceInfoSet serviceInfoSet) {
        int size = serviceInfoSet.size();
        int i = random.nextInt(size);
        int select = 0;
        for (ServiceInfo serviceInfo : serviceInfoSet) {
            if (i == select) {
                return serviceInfo;
            }
            select++;
        }
        return null;
    }
}
