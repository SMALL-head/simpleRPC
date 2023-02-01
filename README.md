# simpleRPC
一个无比简单的RPC框架


## v1（开发中）
### 模块结构
rpc-common 一切非核心的类定义，例如工具类、常量、枚举、pojo <br>
rpc-core 包含注册中心、服务提供方、服务消费方的实现

另外的模块供测试使用（因为使用到了netty，JUnits单元测试的netty尽在使用EmbeddedChannel的时候比较方便，直接上启动服务器的测试不太方便）

### 功能
1. 支持RPC客户端向服务端的远程调用
2. 客户端、服务端实现方式为jdk原生动态代理(InvocationHandler)
3. 支持注册中心，实现最简单的心跳检查，不支持持久化
4. 通信服务技术选型：Netty
5. 方法暂不支持可变参数
