package com.zyc.service_server;

import com.zyc.constants.Constants;
import com.zyc.entity.registry.SocketInfo;
import com.zyc.rpc.registry.config.RegistryConfig;
import com.zyc.rpc.server.RpcServer;
import com.zyc.rpc.server.ServiceProvider;
import com.zyc.service.MyService;
import com.zyc.service.MyServiceImpl;

import java.util.concurrent.ConcurrentHashMap;

public class RpcServerVersion1 {
    public static void main(String[] args) throws Exception {
        // 配置服务提供方的服务器
        RpcServer rpcServer = new RpcServer("127.0.0.1", 8080);
        // 配置全局注册中心的信息
        RegistryConfig.setSocketInfo(new SocketInfo(Constants.LOCALHOST, 8088));
        // 创建一个服务
        MyService myService = new MyServiceImpl();
        ServiceProvider<MyService> provider1 = new ServiceProvider<>(myService, "service");
        rpcServer.addService(provider1);
        // 启动服务器
        rpcServer.startServer();
    }
}
