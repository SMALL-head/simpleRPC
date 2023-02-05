package com.zyc.rpc.server;

import com.zyc.entity.rpc.RpcRequest;
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

    public GenericReturn callService(RpcRequest request) {
        return callService(request.getMsgID(), request.getServiceMethod(), request.getParams(), request.getParamsType());
    }

    public GenericReturn callService(String msgID, String methodName, Object[] params, Class<?>[] paramTypes) {
        Object retValue = null;
        Method method = null;
//        boolean invokeFlag = false;
//        for (Method m : service.getClass().getDeclaredMethods()) {
//            try {
//                retValue = m.invoke(service, params);
//                invokeFlag = true;
//            } catch (Exception e) {
//                // do nothing
//            }
//            if (invokeFlag) {
//                log.debug("[ServiceProvider]-[callService]-成功匹配方法{},参数列表类型为{}", m.getName(), m.getParameterTypes());
//                break;
//            }
//        }
//        if (retValue == null) {
//            log.info("[ServiceProvider]-[callService]-方法匹配及调用过程出现错误");
//            throw new RpcException(RpcErrorEnum.NO_METHOD_MATCH, "");
//        }
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
        return new GenericReturn(msgID, retType, retValue);
    }
}
