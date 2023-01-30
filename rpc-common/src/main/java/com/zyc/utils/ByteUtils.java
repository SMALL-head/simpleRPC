package com.zyc.utils;

public class ByteUtils {
    /**
     * 返回长度为4的byte数组，按大端法存储
     * 例如
     * @param i 需要转换的int变量
     * @return 返回长度为4的byte数组，按大端法存储
     */
    public static byte[] int2byteArray(int i) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte) (i & 0xff);
        bytes[2] = (byte) ((i >> 8) & 0xff);
        bytes[1] = (byte) ((i >> 16) & 0xff);
        bytes[0] = (byte) ((i >> 24) & 0xff);
        return bytes;
    }
    public static byte[] int2byteArray(int i, boolean littleEnd) {
        if (littleEnd) {
            // 小端法
            byte[] bytes = new byte[4];
            bytes[0] = (byte) (i & 0xff);
            bytes[1] = (byte) ((i >> 8) & 0xff);
            bytes[2] = (byte) ((i >> 16) & 0xff);
            bytes[3] = (byte) ((i >> 24) & 0xff);
            return bytes;
        } else {
            return int2byteArray(i);
        }
    }
    public static byte[] short2byteArray(short i) {
        byte[] bytes = new byte[2];
        bytes[1] = (byte) (i & 0xff);
        bytes[0] = (byte) ((i >> 8) & 0xff);
        return bytes;
    }
    public static byte[] short2byteArray(short i, boolean littleEnd) {
        if (littleEnd) {
            // 小端法
            byte[] bytes = new byte[4];
            bytes[0] = (byte) (i & 0xff);
            bytes[1] = (byte) ((i >> 8) & 0xff);
            return bytes;
        } else {
            return short2byteArray(i);
        }
    }
}
