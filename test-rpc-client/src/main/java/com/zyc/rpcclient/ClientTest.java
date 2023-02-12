package com.zyc.rpcclient;

import com.zyc.constants.Constants;
import com.zyc.entity.registry.SocketInfo;
import com.zyc.rpc.client.ServiceConsumer;
import com.zyc.rpc.registry.config.RegistryConfig;
import com.zyc.service.MyService;
import com.zyc.service.MyServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ClientTest {
    public static void main(String[] args) throws InterruptedException {
        // 配置注册中心地址
        RegistryConfig.setSocketInfo(new SocketInfo(Constants.LOCALHOST, 8088));

        // 声明消费者，并指明对应的服务提供方的名字
        ServiceConsumer<MyService> consumerWithAnnotation = new ServiceConsumer<>(MyService.class, "abc");
        ServiceConsumer<MyService> consumer = new ServiceConsumer<>(MyService.class, "service1");
        // 获取执行代理
        MyService serviceProxy1 = consumerWithAnnotation.getServiceProxy();
        MyService serviceProxy2 = consumer.getServiceProxy();

        // 调用远程服务
        int add1 = serviceProxy1.add(1, 2);
        int add2 = serviceProxy2.add(2,3);
//        TimeUnit.SECONDS.sleep(10);
        log.info("[rpc调用结果]-add1={}, add2={}", add1, add2);

    }
}
