package com.zyc.rpcclient;

import com.zyc.constants.Constants;
import com.zyc.entity.registry.SocketInfo;
import com.zyc.rpc.client.ServiceConsumer;
import com.zyc.rpc.registry.config.RegistryConfig;
import com.zyc.service.MyService;
import com.zyc.service.MyServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ClientTest {
    public static void main(String[] args) throws InterruptedException {
        RegistryConfig.setSocketInfo(new SocketInfo(Constants.LOCALHOST, 8088));
        ServiceConsumer<MyService> consumer = new ServiceConsumer<>(MyService.class, "service");

        MyService serviceProxy = consumer.getServiceProxy();
        int add = serviceProxy.add(1, 2);
        log.info("[rpc调用结果]-add={}", add);

    }
}
