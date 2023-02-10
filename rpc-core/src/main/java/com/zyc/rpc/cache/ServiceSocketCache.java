package com.zyc.rpc.cache;

import com.zyc.entity.registry.SocketInfo;

import java.net.Socket;

public interface ServiceSocketCache {
    void cache(String key, SocketInfo value);

    SocketInfo remove(String key);

    SocketInfo find(String key);

}
