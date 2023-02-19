package com.zyc.rpc.loadbalence;

import com.zyc.rpc.server.ServiceProvider;

public interface LoadBalanceStrategy {
    ServiceProvider<?> select();
}
