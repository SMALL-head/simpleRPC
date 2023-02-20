package com.zyc.rpc.loadbalence;

import com.zyc.entity.registry.ServiceInfo;
import com.zyc.entity.registry.collections.ServiceInfoSet;
import com.zyc.rpc.loadbalence.impl.RandomLoadBalanceStrategy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoadBalancer {
    LoadBalanceStrategy strategy;

    ServiceInfoSet serviceInfoSet;

    public ServiceInfo select() {
        ServiceInfo select = strategy.select(serviceInfoSet);
        log.info("[select]-负载均衡选择对象{}", select);
        return select;
    }

    /**
     * 默认构造函数选择的负载均衡策略为：随机负载均衡
     */
    public LoadBalancer() {
        strategy = new RandomLoadBalanceStrategy();
    }

    public LoadBalancer(LoadBalanceStrategy strategy, ServiceInfoSet serviceInfoSet) {
        this.strategy = strategy;
        this.serviceInfoSet = serviceInfoSet;
    }

    public LoadBalanceStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(LoadBalanceStrategy strategy) {
        this.strategy = strategy;
    }

    public ServiceInfoSet getServiceInfoSet() {
        return serviceInfoSet;
    }

    public void setServiceInfoSet(ServiceInfoSet serviceInfoSet ) {
        this.serviceInfoSet = serviceInfoSet;
    }
}
