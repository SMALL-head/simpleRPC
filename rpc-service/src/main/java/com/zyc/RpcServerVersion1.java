package com.zyc;

import com.zyc.constants.Constants;
import com.zyc.entity.registry.SocketInfo;
import com.zyc.rpc.server.RpcServer;
import com.zyc.rpc.server.ServiceProvider;
import com.zyc.service.MyService;
import com.zyc.service.MyServiceImpl;

import java.util.concurrent.ConcurrentHashMap;

public class RpcServerVersion1 {
    public static void main(String[] args) throws Exception {
        RpcServer rpcServer = new RpcServer("127.0.0.1", 8080);
        rpcServer.setRegistrySocketInfo(new SocketInfo(Constants.LOCALHOST, 8088));
        MyService myService = new MyServiceImpl();
        ServiceProvider<MyService> provider1 = new ServiceProvider<>(myService);

        ConcurrentHashMap<String, ServiceProvider<?>> map = new ConcurrentHashMap<>();
        map.put(MyService.class.getCanonicalName(), provider1);
        rpcServer.setServiceProviderMap(map);

        rpcServer.startServer();
    }
}
