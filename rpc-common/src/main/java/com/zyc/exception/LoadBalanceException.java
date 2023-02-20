package com.zyc.exception;

public class LoadBalanceException extends RuntimeException{

    public LoadBalanceException(String msg) {
        super("负载均衡策略执行出错：" + msg);
    }

}
