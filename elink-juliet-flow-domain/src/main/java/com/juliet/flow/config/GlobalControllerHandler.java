package com.juliet.flow.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author xujianjie
 * @date 2023-05-04
 */
@Aspect
@Component
@Slf4j
public class GlobalControllerHandler {

    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping) || @annotation(org.springframework.web.bind.annotation.PostMapping) || @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        if (null == sra) {
            throw new RuntimeException("invalid request");
        }
        HttpServletRequest request = sra.getRequest();
//        HttpServletResponse response = sra.getResponse();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("uri=").append(request.getRequestURI());
        try {
            stringBuilder.append("||args=").append(JSON.toJSONString(point.getArgs()));
        } catch (Exception e) {
            log.error("error print args.", e);
        }
        System.out.println(stringBuilder);
        log.info(stringBuilder.toString());
        Object result;
        try {
            result = point.proceed();
            return result;
        } catch (Throwable ab) {
            throw ab;
        }
    }
}
