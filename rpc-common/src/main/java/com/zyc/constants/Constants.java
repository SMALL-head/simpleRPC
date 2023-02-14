package com.zyc.constants;

public class Constants {
    /**
     * 本机地址
     */
    public final static String LOCALHOST = "127.0.0.1";

    /**
     * 换行符
     */
    public final static String LINE = "\n";

    /**
     * 注册中心通信协议版本1
     */
    public final static byte PROTOCOL_VERSION = (byte) 0x1;

    /**
     * 注册中心通信协议魔数
     */
    public static final int MAGIC_NUMBER = 0x123;

    public static final int REGISTRY_CHECK_INTERVAL = 20;
}
