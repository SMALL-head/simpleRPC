package com.zyc.service;

import com.zyc.annotations.ServiceReference;

@ServiceReference("abc")
public class Myservice11Impl implements MyService{

    @Override
    public int add(int a, int b) {
        System.out.println("abc被调用了");
        return a+b;
    }
}
