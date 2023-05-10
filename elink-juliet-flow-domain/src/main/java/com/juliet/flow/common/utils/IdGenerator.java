package com.juliet.flow.common.utils;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
public class IdGenerator {

    static SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker(1L, 6L);

    public static long getId() {
        return snowflakeIdWorker.nextId();
    }
}
