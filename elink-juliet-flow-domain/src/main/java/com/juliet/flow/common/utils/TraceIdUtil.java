package com.juliet.flow.common.utils;

import java.util.UUID;

/**
 * @author xujianjie
 * @date 2023-08-02
 */
public class TraceIdUtil {

    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<String>();

    public static String getTraceId() {
        if(TRACE_ID.get() == null) {
            String s = UUID.randomUUID().toString();
            setTraceId(s);
        }
        return TRACE_ID.get();
    }

    public static void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }
}
