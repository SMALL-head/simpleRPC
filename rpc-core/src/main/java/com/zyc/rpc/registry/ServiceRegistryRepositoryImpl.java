package com.zyc.rpc.registry;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistryRepositoryImpl implements ServiceRegistry {
    private ConcurrentHashMap<String, Object> registeredServiceMethod = new ConcurrentHashMap<>();
    private Set<String> registeredService = ConcurrentHashMap.newKeySet();
    @Override
    public <T> boolean registry(T service) {
        // todo: 实现注册功能
        return false;
    }

    @Override
    public Object getService(String serviceName) {
        // todo：实现查询功能
        return null;
    }
}
