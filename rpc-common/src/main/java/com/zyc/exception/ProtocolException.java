package com.zyc.exception;

import com.zyc.enums.ProtocolErrorEnum;

public class ProtocolException extends Exception {

    public ProtocolException(ProtocolErrorEnum e) {
        super(e.getDesc());
    }

}
