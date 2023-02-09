package com.zyc.rpc.registry;

import com.zyc.entity.registry.SocketInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class InMemoryServiceRegistryImpl implements ServiceRegistry {
    private final ConcurrentHashMap<String, SocketInfo> registeredServiceMap = new ConcurrentHashMap<>();

    public <T extends Class<?>> boolean registry(T service, SocketInfo socketInfo) {
        registeredServiceMap.put(service.getCanonicalName(), socketInfo);
        log.info("[InMemoryServiceRegistryImpl.registry]-服务{}向注册中心注册，socket地址为{}:{}"
            ,service, socketInfo.getHost(), socketInfo.getPort());
        return true;
    }

    @Override
    public boolean registry(String service, String host, int port) {
        registeredServiceMap.put(service, new SocketInfo(host, port));
        log.info("[InMemoryServiceRegistryImpl.registry]-服务{}向注册中心注册，socket地址为{}:{}", service, host, port);
        return true;
    }

    @Override
    public SocketInfo getServiceAddr(String serviceName) {
        return registeredServiceMap.get(serviceName);
    }

    @Override
    public boolean offlineService(String serviceName) {
        SocketInfo remove = registeredServiceMap.remove(serviceName);
        if (remove == null) {
            log.warn("[offlineService]-未查找到名为{}的服务", serviceName);
            return false;
        }
        return true;
    }
}
