package com.zyc.rpc.cache;

import com.zyc.entity.registry.SocketInfo;
import com.zyc.entity.registry.collections.ServiceInfoSet;

import java.net.Socket;

public interface ServiceSocketCache {
    void cache(String key, ServiceInfoSet value);

    ServiceInfoSet remove(String key);

    ServiceInfoSet find(String key);

}
