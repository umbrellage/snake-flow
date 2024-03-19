package com.juliet.flow.client.common.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * @author xujianjie
 * @date 2023-05-18
 */
public class ThreadPoolFactory {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolFactory.class);

    public static final ExecutorService THREAD_POOL_TODO_MAIN = new ThreadPoolExecutor(10,
            100,
            1,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(1000),
            new ThreadFactory("elink", "flow-main"),
            new ElinkRejectedExecutionHandler());

    public static final ExecutorService THREAD_POOL_CACHE = new ThreadPoolExecutor(10,
            100,
            1,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(1000),
            new ThreadFactory("elink", "flow-cache"),
            new ElinkRejectedExecutionHandler());


    static class ElinkRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.error("thread pool is full, executor:{}", executor);
        }
    }

    public static <T> T get(Future<T> f) {
        return get(f, 10000);
    }

    public static <T> T get(Future<T> f, int timeOut) {
        if (f == null) {
            return null;
        }
        try {
            return f.get(timeOut, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Fail to get value from future: ", e);
            return null;
        }
    }
}
