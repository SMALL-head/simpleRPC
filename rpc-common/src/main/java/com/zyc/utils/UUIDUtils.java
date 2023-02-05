package com.zyc.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * 生成全局唯一id的工具类
 */
@Slf4j
public class UUIDUtils {
    static public String getUUiD() {
        String ret;
        log.debug("生成UUID:{}", (ret = UUID.randomUUID().toString()));
        return ret;
    }
}
