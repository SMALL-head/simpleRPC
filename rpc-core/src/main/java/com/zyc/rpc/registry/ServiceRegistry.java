package com.zyc.rpc.registry;

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

    boolean offlineService(String serviveName);
}
