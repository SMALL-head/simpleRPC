package com.zyc.entity.rpc;

import java.io.Serializable;

public class GenericReturn implements Serializable {
    Class<?> returnClass;
    Object value;

    public GenericReturn(Class<?> returnClass, Object value) {
        this.returnClass = returnClass;
        this.value = value;
    }

    public Class<?> getReturnClass() {
        return returnClass;
    }

    public void setReturnClass(Class<?> returnClass) {
        this.returnClass = returnClass;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
