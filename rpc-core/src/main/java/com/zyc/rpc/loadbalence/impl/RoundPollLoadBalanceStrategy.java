package com.zyc.rpc.loadbalence.impl;

import com.zyc.entity.registry.ServiceInfo;
import com.zyc.entity.registry.collections.ServiceInfoSet;
import com.zyc.exception.LoadBalanceException;
import com.zyc.rpc.loadbalence.LoadBalanceStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * todo: 轮询算法缺少必要的组件，pollingPointer应该每个ServiceInfoSet对应一个，看看代码应该如何修改策略的结构
 * 维护serviceName-pointer的map就好
 */
public class RoundPollLoadBalanceStrategy implements LoadBalanceStrategy {
    /**
     * key为serviceName，value为指针
     */
    Map<String, Integer> pollingPointerMap = new HashMap<>();
    @Override
    public ServiceInfo select(ServiceInfoSet serviceInfoSet) {
        List<ServiceInfo> serviceInfos = serviceInfoSet.stream().toList();
        Integer pointer = pollingPointerMap.get(serviceInfoSet.getServiceName());
        if (pointer == null) {
            throw new LoadBalanceException("轮询指针为null");
        }
        ServiceInfo serviceInfo = serviceInfos.get(pointer);
        pointer = (pointer + 1) % serviceInfos.size();
        pollingPointerMap.put(serviceInfoSet.getServiceName(), pointer);
        return serviceInfo;
    }
}
