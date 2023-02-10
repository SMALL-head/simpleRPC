package com.zyc.registry;

import com.zyc.constants.Constants;
import com.zyc.rpc.registry.ServiceRegistryCenter;

public class RegistryCenterServer {
    public static void main(String[] args) {
        ServiceRegistryCenter center = new ServiceRegistryCenter(Constants.LOCALHOST, 8088);
        center.serverStart();
    }
}
