package com.zyc.rpc.client;

public class RpcClient {
    final static String LOCALHOST = "127.0.0.1";
    String host;
    int port;
    // todo：rpc调用的client过程尚未开始

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public RpcClient() {
        this.host = LOCALHOST;
        this.port = 8080;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
