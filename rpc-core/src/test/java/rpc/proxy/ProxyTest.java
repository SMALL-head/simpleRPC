package rpc.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyTest {
    interface TestClassInterface {
        void method1();
    }
    static class TestClass implements TestClassInterface{
        @Override
        public void method1() {
            System.out.println("method1 被调用");
        }
    }
    static class CglibTestClass {
        public int method1(int a, int b) {
            System.out.println("method1方法被调用");
            return (a + b) / (a);
        }
    }

    static class CglibProxy implements MethodInterceptor {
        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            System.out.println("增强方法前");
            System.out.println("---------------源方法实现-----------------");
            Object invoke = method.invoke(o, objects);
            methodProxy.invokeSuper(o, objects);
            methodProxy.invoke(o, objects);
            System.out.println("---------------源方法实现结束--------------");
            return invoke;
        }
    }
    @Test
    public void jdk_proxy_test1() {
        TestClass testClass = new TestClass();
        TestClassInterface testClassProxy = (TestClassInterface) Proxy.newProxyInstance(testClass.getClass().getClassLoader(), testClass.getClass().getInterfaces(), new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("before");
                Object ret = method.invoke(testClass, args);
                System.out.println("返回值:" + method.getReturnType());
                System.out.println("after");
                return ret;
            }
        });

        testClassProxy.method1();
    }

    @Test
    public void cglib_proxy_test2() {
        Enhancer e = new Enhancer();
        e.setSuperclass(CglibTestClass.class);
    }
}
