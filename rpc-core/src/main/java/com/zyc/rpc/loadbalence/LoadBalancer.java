package com.zyc.rpc.loadbalence;

import com.zyc.rpc.server.ServiceProvider;

import java.util.List;

public class LoadBalancer {
    LoadBalanceStrategy strategy;

    List<ServiceProvider<?>> serviceProviderList;

    public LoadBalancer(LoadBalanceStrategy strategy, List<ServiceProvider<?>> serviceProviderList) {
        this.strategy = strategy;
        this.serviceProviderList = serviceProviderList;
    }

    public LoadBalanceStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(LoadBalanceStrategy strategy) {
        this.strategy = strategy;
    }
}
