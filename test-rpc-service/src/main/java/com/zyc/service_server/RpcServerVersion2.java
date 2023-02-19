package com.zyc.service_server;

import com.zyc.constants.Constants;
import com.zyc.entity.registry.SocketInfo;
import com.zyc.rpc.registry.config.RegistryConfig;
import com.zyc.rpc.server.RpcServer;
import com.zyc.rpc.server.ServiceProvider;
import com.zyc.service.MyService;
import com.zyc.service.MyServiceImpl;

/**
 * 和RpcServerVersion1同时启动，用于测试负载均衡是否能够正常触发
 */
public class RpcServerVersion2 {
    public static void main(String[] args) throws Exception {
        // 1. 配置服务提供方的服务器
        RpcServer rpcServer = new RpcServer("127.0.0.1", 8086);
        // 2. 配置全局注册中心的信息
        RegistryConfig.setSocketInfo(new SocketInfo(Constants.LOCALHOST, 8088));
        // 3. 创建两个服务
        MyService myService1 = new MyServiceImpl();
        MyService myService2 = new MyServiceImpl();
        ServiceProvider<MyService> provider1 = new ServiceProvider<>(myService1, "service1");
        rpcServer.addService(provider1);
//        rpcServer.addService(provider2);

        // 4. 启动服务器，启动后将会自动把这两个服务注册到注册中心
        rpcServer.startServer(RpcServerVersion2.class);
    }
}
