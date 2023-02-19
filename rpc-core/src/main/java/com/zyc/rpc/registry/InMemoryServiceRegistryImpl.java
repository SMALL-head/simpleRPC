package com.zyc.rpc.registry;

import com.zyc.constants.Constants;
import com.zyc.entity.registry.ServiceInfo;
import com.zyc.entity.registry.SocketInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class InMemoryServiceRegistryImpl implements ServiceRegistry {
    private final ConcurrentHashMap<String, ServiceInfo> registeredServiceMap = new ConcurrentHashMap<>();

    public <T extends Class<?>> boolean registry(T service, SocketInfo socketInfo) {
        registeredServiceMap.put(service.getCanonicalName(), new ServiceInfo(socketInfo, Calendar.getInstance()));
        log.info("[InMemoryServiceRegistryImpl.registry]-服务{}向注册中心注册，socket地址为{}:{}"
            , service, socketInfo.getHost(), socketInfo.getPort());
        return true;
    }

    @Override
    public boolean registry(String service, String host, int port) {
        registeredServiceMap.put(service, new ServiceInfo(new SocketInfo(host, port), Calendar.getInstance()));
        log.info("[InMemoryServiceRegistryImpl.registry]-服务{}向注册中心注册，socket地址为{}:{}", service, host, port);
        return true;
    }

    @Override
    public SocketInfo getServiceAddr(String serviceName) {
        return registeredServiceMap.get(serviceName).getSocketInfo();
    }

    @Override
    public boolean offlineService(String serviceName) {
        ServiceInfo remove = registeredServiceMap.remove(serviceName);
        if (remove == null) {
            log.warn("[offlineService]-未查找到名为{}的服务", serviceName);
            return false;
        }
        return true;
    }

    @Override
    public boolean updateLastUpdate(String serviceName) {
        ServiceInfo serviceInfo = registeredServiceMap.get(serviceName);
        if (serviceInfo == null) {
            log.error("[update]-未找到名为{}的服务", serviceName);
            return false;
        }
        serviceInfo.setLastUpdate(Calendar.getInstance());
        return true;
    }

    @Override
    public void removeDeadService() {
        // 1. 计算上一次检查的时间
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, -1 * Constants.REGISTRY_CHECK_INTERVAL);

        // 2. 超时未更新的服务算为死亡服务，删除
        for (String serviceName : registeredServiceMap.keySet()) {
            ServiceInfo service = registeredServiceMap.get(serviceName);
            if (service.getLastUpdate().compareTo(instance) < 0) {
                // 当前service更新时间小于上一次检查的时间，代表这个服务的心跳包没有收到，也就说服务异常
                log.warn("[removeDeadService]-检测到{}更新时间不对，下线该服务", serviceName);
                registeredServiceMap.remove(serviceName);
            }
        }
    }
}
