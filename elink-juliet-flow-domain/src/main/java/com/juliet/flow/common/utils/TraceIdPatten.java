package com.juliet.flow.common.utils;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * @author xujianjie
 * @date 2023-08-19
 */
public class TraceIdPatten extends ClassicConverter {

    @Override
    public String convert(ILoggingEvent iLoggingEvent) {
        return TraceIdUtil.getTraceId();
    }
}
