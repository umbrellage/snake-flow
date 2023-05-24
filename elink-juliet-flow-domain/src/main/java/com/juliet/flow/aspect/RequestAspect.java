package com.juliet.flow.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 这个类用来对输入进行一些校验
 *
 * @author Geweilang
 */
@Aspect
@Component
public class RequestAspect {

    @Around(value = "within(com.juliet.flow.controller.*)")
    public Object checkAndPerformance(ProceedingJoinPoint point) throws Throwable {
        return ElinkMonitor.performance(point);
    }
}
