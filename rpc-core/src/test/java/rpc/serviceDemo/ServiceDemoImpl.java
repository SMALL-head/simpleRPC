package rpc.serviceDemo;

public class ServiceDemoImpl implements ServiceDemo{
    @Override
    public int add(int a, int b) {
        System.out.println("调用add方法");
        return a + b;
    }

    @Override
    public String concat(String s) {
        System.out.println("调用concat方法");
        return s;
    }
}
