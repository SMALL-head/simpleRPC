package com.zyc.rpc.client;

import com.zyc.entity.registry.RpcRegistryRequest;
import com.zyc.entity.registry.RpcRegistryResponse;
import com.zyc.entity.registry.SocketInfo;
import com.zyc.entity.rpc.GenericReturn;
import com.zyc.entity.rpc.RpcRequest;

import java.util.concurrent.CompletableFuture;

public interface RpcClient {

    CompletableFuture<RpcRegistryResponse> sendRegistryRequest(RpcRegistryRequest request);

    CompletableFuture<GenericReturn> sendRpcRequest(RpcRequest request, SocketInfo serviceProviderSocketInfo);
}
