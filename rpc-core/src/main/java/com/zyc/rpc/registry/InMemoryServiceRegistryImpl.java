package com.zyc.rpc.registry;

import com.zyc.constants.Constants;
import com.zyc.entity.registry.ServiceInfo;
import com.zyc.entity.registry.SocketInfo;
import com.zyc.entity.registry.collections.ServiceInfoSet;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Slf4j
public class InMemoryServiceRegistryImpl implements ServiceRegistry {
    private final ConcurrentHashMap<String, ServiceInfoSet> registeredServiceMap = new ConcurrentHashMap<>();

    public <T extends Class<?>> boolean registry(T service, SocketInfo socketInfo) {
        ServiceInfoSet serviceInfos = registeredServiceMap.get(service.getCanonicalName());
        if (serviceInfos == null) {
            // 没有任何信息，需要新的set
            serviceInfos = new ServiceInfoSet(service.getCanonicalName());
            serviceInfos.add(new ServiceInfo(socketInfo, Calendar.getInstance()));
            registeredServiceMap.put(service.getCanonicalName(), serviceInfos);
        } else {
            serviceInfos.add(new ServiceInfo(socketInfo, Calendar.getInstance()));
        }
//        registeredServiceMap.put(service.getCanonicalName(), new ServiceInfo(socketInfo, Calendar.getInstance()));
        log.info("[InMemoryServiceRegistryImpl.registry]-服务{}向注册中心注册，socket地址为{}:{}"
            , service, socketInfo.getHost(), socketInfo.getPort());
        return true;
    }

    @Override
    public boolean registry(String service, String host, int port) {
        ServiceInfoSet serviceInfos = registeredServiceMap.get(service);
        SocketInfo socketInfo = new SocketInfo(host, port);
        if (serviceInfos == null) {
            // 没有任何信息，需要新的set
            serviceInfos = new ServiceInfoSet(service);
            serviceInfos.add(new ServiceInfo(socketInfo, Calendar.getInstance()));
            registeredServiceMap.put(service, serviceInfos);
        } else {
            serviceInfos.add(new ServiceInfo(socketInfo, Calendar.getInstance()));
        }
//        registeredServiceMap.put(service, new ServiceInfo(new SocketInfo(host, port), Calendar.getInstance()));
        log.info("[registry]-服务{}向注册中心注册，socket地址为{}:{}", service, host, port);
        return true;
    }

    @Override
    public Set<ServiceInfo> getServiceAddr(String serviceName) {
        return registeredServiceMap.get(serviceName);
    }

    @Override
    public boolean offlineService(String serviceName, String host, int port) {
        Set<ServiceInfo> serviceInfos = registeredServiceMap.get(serviceName);
        if (serviceInfos == null) {
            log.warn("[offlineService]-未找到{}对应的任何注册信息", serviceName);
            return true;
        }
        boolean remove = serviceInfos.remove(new ServiceInfo(new SocketInfo(host, port), null));
//        ServiceInfo remove = registeredServiceMap.remove(serviceName);
        if (!remove) {
            log.warn("[offlineService]-未查找到名为{}的服务", serviceName);
            return false;
        }
        return true;
    }

    @Override
    public boolean updateLastUpdate(String serviceName, String host, int port) {
        ServiceInfoSet serviceInfos = registeredServiceMap.get(serviceName);
        if (serviceInfos == null) {
            log.error("[updateLastUpdate]-未找到名为{}的服务集群", serviceName);
            return false;
        }

        for (ServiceInfo serviceInfo : serviceInfos) {
            if (serviceInfo.equals(new ServiceInfo(new SocketInfo(host, port), null))) {
                serviceInfo.setLastUpdate(Calendar.getInstance());
                return true;
            }
        }
        log.error("[update]-服务集群{}内不存在地址为{}:{}的指定服务", serviceName, host, port);
        return false;
    }

    @Override
    public void removeDeadService() {
        // 1. 计算上一次检查的时间
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, -1 * Constants.REGISTRY_CHECK_INTERVAL);

        // 2. 超时未更新的服务算为死亡服务，删除
        for (String serviceName : registeredServiceMap.keySet()) {
            ServiceInfoSet serviceInfos = registeredServiceMap.get(serviceName);
//            ServiceInfo service = (ServiceInfo) serviceInfos;
            serviceInfos.removeIf(serviceInfo -> {
                // 当前service更新时间小于上一次检查的时间，代表这个服务的心跳包没有收到，也就说服务异常
                boolean b = serviceInfo.getLastUpdate().compareTo(instance) < 0;
                if (b) {
                    log.warn("[removeDeadService]-检测到{}更新时间不对，下线该服务", serviceName);
                }
                return b;
            });
        }
    }
}
