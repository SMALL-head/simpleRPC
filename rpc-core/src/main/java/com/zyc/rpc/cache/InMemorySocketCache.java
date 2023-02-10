package com.zyc.rpc.cache;

import com.zyc.entity.registry.SocketInfo;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 单例模式，所有消费者都公用一个cache，节约空间
 *
 * @apiNote 使用getInstance获取单例
 */
public class InMemorySocketCache implements ServiceSocketCache {
    ConcurrentHashMap<String, SocketInfo> cache;

    static final InMemorySocketCache singleton = new InMemorySocketCache();

    /**
     * 单例模式
     */
    private InMemorySocketCache() {
        cache = new ConcurrentHashMap<>();
    }

    static public InMemorySocketCache getInstance() {
        return singleton;
    }

    @Override
    public void cache(String key, SocketInfo value) {
        cache.put(key, value);
    }

    @Override
    public SocketInfo remove(String key) {
        return cache.remove(key);
    }@Override
    public SocketInfo find(String key) {
        return cache.get(key);
    }

    public int cacheSize() {
        return cache.size();
    }

    public void clear() {
        cache.clear();
    }
}
