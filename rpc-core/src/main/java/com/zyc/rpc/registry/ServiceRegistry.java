package com.zyc.rpc.registry;

import com.zyc.entity.registry.ServiceInfo;
import com.zyc.entity.registry.SocketInfo;

/**
 * 拥有注册服务能力和获取已经注册的服务的SocketAddr的能力的接口
 * 为了扩展性而诞生的接口，之后可能会实现第三方的注册中心
 */
public interface ServiceRegistry {
    /**
     * 向注册中心注册服务
     * @param service 通过方法名进行注册，同时将该service的host和port记录
     * @return 注册成功则返回true；注册失败则返回false
     */
    boolean registry(String service, String host, int port);

    /**
     * @return 返回对应的service
     */
    SocketInfo getServiceAddr(String serviceName);

    /**
     * 下线服务
     * @param serviceName 服务名
     * @return 成功下线，返回true；否则返回false
     */
    boolean offlineService(String serviceName);

    /**
     * 接收到心跳包后更新服务存活时间
     * @param serviceName 服务名
     * @return 更新成功返回true，否则返回false
     */
    boolean updateLastUpdate(String serviceName);

    /**
     * 由注册中心的定时任务定时执行的操作，其目的是为了删除不满足条件的任务
     */
    void removeDeadService();
}
