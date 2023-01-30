package com.zyc.rpc.registry;

import com.zyc.entity.registry.SocketInfo;

import java.util.concurrent.ConcurrentHashMap;

public class InMemoryServiceRegistryImpl implements ServiceRegistry {
    private final ConcurrentHashMap<String, SocketInfo> registeredServiceMap = new ConcurrentHashMap<>();

    public <T extends Class<?>> boolean registry(T service, SocketInfo socketInfo) {
        registeredServiceMap.put(service.getCanonicalName(), socketInfo);
        return true;
    }

    @Override
    public boolean registry(String service, String host, int port) {
        registeredServiceMap.put(service, new SocketInfo(host, port));
        return true;
    }

    @Override
    public SocketInfo getServiceAddr(String serviceName) {
        // todo：实现查询功能
        registeredServiceMap.get(serviceName);
        return null;
    }
}
