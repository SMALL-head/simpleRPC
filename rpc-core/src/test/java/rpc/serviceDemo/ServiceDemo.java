package rpc.serviceDemo;

import java.io.Serializable;

public interface ServiceDemo extends Serializable {
    int add(int a, int b);

    String concat(String s);
}
