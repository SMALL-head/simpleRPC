package com.zyc.rpc.server;

import com.zyc.enums.RpcErrorEnum;
import com.zyc.exception.RpcException;
import com.zyc.entity.rpc.GenericReturn;
import lombok.extern.slf4j.Slf4j;

import java.io.ObjectStreamException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 服务提供者，一个service对应一个service
 *
 * @param <T>服务类型
 */
@Slf4j
public class ServiceProvider<T> {
    T service;

    public ServiceProvider(T service) {
        this.service = service;
    }

    public GenericReturn callService(String methodName, Object[] params, Class<?>[] paramTypes) {
        Object retValue = null;
        Method method;
        try {
            log.info("[ServiceProvider]-[callService]-匹配函数{}-参数类型{}", methodName, paramTypes);
            method = service.getClass().getMethod(methodName, paramTypes);
            log.info("[ServiceProvider]-[callService]-匹配函数{}-参数类型{}-匹配成功", methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            throw new RpcException(RpcErrorEnum.NO_METHOD_MATCH, "");
        }
        try {
            retValue = method.invoke(service, params);
        } catch (Exception e) {
            throw new RpcException(RpcErrorEnum.PRC_INVOKE_ERROR, "无法调用方法");
        }
//        for (Method m : service.getClass().getMethods()) {
//            log.info("[ServiceProvider]-[callService]-方法{}匹配中-参数类型{}", m.getName(), m.getParameterTypes());
//            if (m.getName().equals(methodName) && Arrays.equals(m.getParameterTypes(), paramsClass)) {
//                // 方法名和参数类型匹配，就尝试调用
//                try {
//                    retValue = m.invoke(service, params);
//                } catch (Exception e) {
//                    throw new RpcException(RpcErrorEnum.PRC_INVOKE_ERROR, "无法调用方法");
//                }
//
//                Class<?> retType = retValue.getClass();
//                return new GenericReturn(retType, retValue);
//            }
//        }
        // 若没有任何方法匹配，那么抛出异常
        Class<?> retType = retValue.getClass();
        return new GenericReturn(retType, retValue);
    }
}
