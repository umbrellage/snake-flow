package com.juliet.flow.client.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JulietFlowInterceptor {

    /**
     * 流程名称
     */
    String name() default "";

    /**
     * 流程标识
     */
    String code() default "";
}
