package com.zyc.rpc.registry;

public interface ServiceRegistry {
    /**
     * 向注册中心注册服务
     * @param service 其实质应该是是一个类，其会将该类对应的方法均注册
     * @return 注册成功则返回true；注册失败则返回false
     * @param <T> service的类型
     */
    <T> boolean registry(T service);

    /**
     * @return 返回对应的service
     */
    Object getService(String serviceName);
}
