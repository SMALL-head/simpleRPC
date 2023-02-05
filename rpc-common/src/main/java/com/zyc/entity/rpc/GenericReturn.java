package com.zyc.entity.rpc;

import java.io.Serializable;

public class GenericReturn implements Serializable {
    String msgID;
    Class<?> returnClass;
    Object value;

    public GenericReturn(String msgID, Class<?> returnClass, Object value) {
        this.returnClass = returnClass;
        this.value = value;
        this.msgID = msgID;
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

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }
}
