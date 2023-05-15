package com.juliet.flow.aspect;

/**
 * TimeConsuming
 *
 * @author Geweilang
 * @date 2022/8/12
 */
public class TimeConsuming {

    /**
     * 开始时间的毫秒数
     */
    private final long curTime = System.currentTimeMillis();
    /**
     * 上次调用lastConsume的时候
     */
    private long lastTime = System.currentTimeMillis();

    public static TimeConsuming of() {
        return new TimeConsuming();
    }

    /**
     * @return 总耗时
     */
    public long consume() {
        return System.currentTimeMillis() - curTime;
    }

    /**
     * 上次调用lastConsume到现在的耗时
     *
     * @return 分步耗时
     */
    public long lastConsume() {
        long ret = System.currentTimeMillis() - lastTime;
        lastTime = System.currentTimeMillis();

        return ret;
    }
}
