package com.juliet.flow.client.common.thread;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xujianjie
 * @date 2023-05-18
 */
public final class ThreadFactory implements java.util.concurrent.ThreadFactory {

    private static final AtomicInteger poolNumber = new AtomicInteger(1);

    private final ThreadGroup group;

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    private final String namePrefix;

    /**
     * @param groupFlag    任务组
     * @param functionName 功能名
     */
    public ThreadFactory(String groupFlag, String functionName) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        // 在线程名上加上任务组名称，方便调试
        namePrefix = buildPrefix(groupFlag, functionName, poolNumber);
    }

    private String buildPrefix(String vendorFlag, String functionName, final AtomicInteger poolNumber) {
        StringBuffer sb = new StringBuffer("pool-").append(poolNumber.getAndIncrement()).append("-thread-");
        String connChar = "-";
        if (vendorFlag != null && vendorFlag.trim().length() > 0) {
            sb.append(vendorFlag).append(connChar);
        }
        if (functionName != null && functionName.trim().length() > 0) {
            sb.append(functionName).append(connChar);
        }
        return sb.toString();
    }

    @Override
    public Thread newThread(Runnable r) {
        String threadName = namePrefix + threadNumber.getAndIncrement();
        Thread t = new Thread(group, r, threadName, 0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}
