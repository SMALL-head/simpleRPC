# simpleRPC
一个无比简单的RPC框架，但实现了服务提供端，服务调用方，以及注册中心的实现的完整的RPC架构。该项目并不是只为了实现整个流程而制作的，而是能够在导入该项目后能够直接简化rpc的配置而书写的。

## v1 基础功能开发（已完成）
### 模块结构
rpc-common 一切非核心的类定义，例如工具类、常量、枚举、pojo <br>
rpc-core 包含注册中心、服务提供方、服务消费方的实现

另外的模块供测试使用（因为使用到了netty，JUnits单元测试的netty尽在使用EmbeddedChannel的时候比较方便，直接上启动服务器的测试不太方便）

### 功能
1. 支持RPC客户端向服务端的远程调用---已完成
2. 客户端、服务端实现方式为jdk原生动态代理(InvocationHandler)---已完成
3. 支持注册中心---已完成
4. 通信服务技术选型：Netty---已完成
5. 方法支持可变参数---已完成

### 尚未解决的问题
1. 多重实现接口的问题尚未解决，也就是说如果一个接口有两个实现类，那么就不知道该怎么做了：解决思路，设置全局唯一id（唯一name标识）来寻找---已解决

### 运行实例

1. 首先启动注册中心
```java
public static void main(String[] args) {
    ServiceRegistryCenter center = new ServiceRegistryCenter(Constants.LOCALHOST, 8088);
    center.serverStart();
}
```
获取到日志：`注册中心启动，host:xxxxx, port:xxxx`代表启动成功

2. 启动服务提供方
```java
public static void main(String[] args) throws Exception {
    // 1. 配置服务提供方的服务器
    RpcServer rpcServer = new RpcServer("127.0.0.1", 8080);
    // 2. 配置全局注册中心的信息
    RegistryConfig.setSocketInfo(new SocketInfo(Constants.LOCALHOST, 8088));
    // 3. 创建两个服务
    MyService myService1 = new MyServiceImpl();
    MyService myService2 = new MyServiceImpl();
    ServiceProvider<MyService> provider1 = new ServiceProvider<>(myService1, "service1");
    ServiceProvider<MyService> provider2 = new ServiceProvider<>(myService2, "service2");
    rpcServer.addService(provider1);
    rpcServer.addService(provider2);
    
    // 4. 启动服务器，启动后将会自动把这两个服务注册到注册中心
    rpcServer.startServer();
}
```
运行后将会得到服务成功注册的日志

3. 消费方调用
```java
public static void main(String[] args) throws InterruptedException {
    // 配置注册中心地址
    RegistryConfig.setSocketInfo(new SocketInfo(Constants.LOCALHOST, 8088));
    
    // 声明消费者，并指明对应的服务提供方的名字。MyService是自定义的一个接口
    ServiceConsumer<MyService> consumer = new ServiceConsumer<>(MyService.class, "service");
    
    // 获取执行代理
    MyService serviceProxy = consumer.getServiceProxy();
    
    // 调用远程服务
    int add = serviceProxy.add(1, 2);
    log.info("[rpc调用结果]-add={}", add);

}
```
服务提供者的日志中有调用的记录，服务消费者的日志中能够获取到调用返回值
## v2.1 部分功能完善 (已完成)

### 功能
1. 客户端支持本地cache --- 已完成
2. 服务提供方与注册中心的增加心跳检查，若注册中心检测到服务方下线了，那么注销服务 --- 已完成
3. 完成部分基于注解的开发 --- 已完成，服务端可以使用注解配置
~~4. 服务的调用支持同步和异步的方式~~

注解的使用方法如下，可以看到与之前相比大幅度减少代码量：
```java
// 1. 启动类上使用注解指定服务扫描包
@ServiceScan(scan = "com.zyc.service")
public class RpcServerVersion1 {
    public static void main(String[] args) throws Exception {
        // 2. 配置服务提供方的服务器
        RpcServer rpcServer = new RpcServer("127.0.0.1", 8080);
        // 3. 配置全局注册中心的信息
        RegistryConfig.setSocketInfo(new SocketInfo(Constants.LOCALHOST, 8088));
        // 4. 启动服务器，启动后将会自动把这com.zyc.service包下的所有带有@ServiceReference注解的类生成执行代理，并注册到注册中心
        rpcServer.startServer(RpcServerVersion1.class);
    }
}
```
然后在上述指定的路径中定义提供服务的类就行
```java
@ServiceReference("abc")
public class MyServiceImpl implements MyService{
    @Override
    public int add(int a, int b) {
        System.out.println("abc被调用了");
        return a+b;
    }
}
```
## v2.2 增加服务负载均衡（已完成）
同一个服务名可以对应多台机器的inet，比如说端口8081和端口8082同时注册名字相同的服务。消费者在选择服务的时候，需要通过负载均衡的策略进行服务的选择
<br> 此处选择在消费者一端进行负载均衡的原因是：
1. 消费者端有对应服务名的缓存，因此可以说消费者对所有服务是可见的，在这种情况下进行筛选比较方便
2. 其他方案的不好之处：
   1. 服务端的负载均衡（不同机器对另外一台机器不可见，不方便做负载均衡）
   2. 专门的负载均衡机器：增加了请求链路
<br>
因此确定方案选型：消费者端的负载均衡设置
<br>
已实现的负载均衡策略有：
- 完全随机
- 轮询
