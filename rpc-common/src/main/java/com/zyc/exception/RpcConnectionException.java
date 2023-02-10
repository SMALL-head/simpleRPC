package com.zyc.exception;

public class RpcConnectionException extends RuntimeException {
    String host;
    int port;

    public RpcConnectionException(String message, String host, int port) {
        super(message);
        this.host = host;
        this.port = port;
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
