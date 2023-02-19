package com.zyc.rpc.cache;

import com.zyc.entity.registry.ServiceInfo;
import com.zyc.entity.registry.SocketInfo;
import com.zyc.entity.registry.collections.ServiceInfoSet;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单例模式，所有消费者都公用一个cache，节约空间
 *
 * @apiNote 使用getInstance获取单例
 */
public class InMemorySocketCache implements ServiceSocketCache {
    ConcurrentHashMap<String, ServiceInfoSet> cache;

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
    public void cache(String key, ServiceInfoSet value) {
        cache.put(key, value);
    }

    @Override
    public ServiceInfoSet remove(String key) {
        return cache.remove(key);
    }

    @Override
    public ServiceInfoSet find(String key) {
        return cache.get(key);
    }

    public int cacheSize() {
        return cache.size();
    }

    public void clear() {
        cache.clear();
    }
}
