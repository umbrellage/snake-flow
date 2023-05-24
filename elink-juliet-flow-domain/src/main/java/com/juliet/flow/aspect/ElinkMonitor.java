package com.juliet.flow.aspect;

import com.alibaba.fastjson2.JSON;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

/**
 * PctMonitor 请求参数监控类，用于请求URL参数打印，请求响应时间统计
 *
 * @author Geweilang
 * @date 2020/6/21
 */
public class ElinkMonitor {

    public static Object performance(ProceedingJoinPoint point)
        throws Throwable {
        TimeConsuming timeConsuming = TimeConsuming.of();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
            .getRequestAttributes();
        if (attributes == null) {
            logger.error("Servlet request attributes is null.");
            return point.proceed();
        }

        ReqData data = printReqParam(point);

        logger.info("Request start... uuid:{}, url:{},method:{}, body:{}, aop time:{} ms", data.getUuid(), data.getUrl(),
            data.getHttpMethod(), data.getBody(), timeConsuming.consume());

        Object response = point.proceed();

        logger.trace("Response print.uuid:{}, url:{},method:{}, body:{} response:{}",
            data.getUuid(), data.getUrl(), data.getHttpMethod(), data.getBody(), JSON.toJSONString(response));

        int status = attributes.getResponse() != null ? attributes.getResponse().getStatus() : 0;

        logger.info("Performance monitoring, uuid:{}, url:{}, status code:{}, cost time:{} ms ",
            data.getUuid(), data.getUrl(), status, timeConsuming.consume());

        return response;
    }

    private static ReqData printReqParam(ProceedingJoinPoint point) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
            .getRequestAttributes();

        HttpServletRequest request = attributes.getRequest();

        ReqData result = new ReqData();
        result.setHttpMethod(request.getMethod());

        String path = request.getRequestURI();

        MethodSignature methodSignature = (MethodSignature) point.getSignature();

        Method method = methodSignature.getMethod();

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        Annotation[][] parentParamAnnotations = superParameterAnnotations(method);

        Object[] params = point.getArgs();
        String[] paramNames = methodSignature.getParameterNames();

        Set<String> urlParam = new HashSet<>();

        for (int i = 0; i < params.length; ++i) {
            if (params[i] instanceof ServletRequest || params[i] instanceof ServletResponse || params[i] instanceof MultipartFile) {
                //ServletRequest不能序列化，从入参里排除，否则报异常：java.lang.IllegalStateException: It is illegal to call this method if the current request is not in asynchronous mode (i.e. isAsyncStarted() returns false)
                //ServletResponse不能序列化 从入参里排除，否则报异常：java.lang.IllegalStateException: getOutputStream() has already been called for this response
                continue;
            }
            Annotation[] annotations = parameterAnnotations[i];
            if (parentParamAnnotations != null) {
                Annotation[] parentAnn = parentParamAnnotations[i];
                if (parentAnn != null) {
                    if (annotations == null) {
                        annotations = parentAnn;
                    } else {
                        annotations = ArrayUtils.addAll(annotations, parentAnn);
                    }
                }
            }

            if (annotations == null) {
                continue;
            }

            RequestParam requestParam = null;
            RequestBody requestBody = null;


            for (Annotation annotation : annotations) {

                if (annotation instanceof RequestParam) {
                    requestParam = (RequestParam) annotation;
                }
                if (annotation instanceof RequestBody) {
                    requestBody = (RequestBody) annotation;
                }
            }
            if (requestBody != null) {
                result.setBody(params[i]);
            }

            if (requestParam == null) {
                continue;
            }

            String name = requestParam.value();
            if (StringUtils.isBlank(name)) {
                name = requestParam.name();
            }

            if (StringUtils.isBlank(name)) {
                name = paramNames[i];
            }

            urlParam.add(name + "=" + params[i]);
        }

        if (urlParam.isEmpty()) {
            result.setUrl(path);
        } else {
            result.setUrl(path + "?" + String.join("&", urlParam));
        }
        return result;
    }

    @Getter
    @Setter
    static class ReqData {

        private String uuid = UUID.randomUUID().toString();
        private String url;
        private String httpMethod;
        private Object body;
        private Object requestBody;
    }

    public static Annotation[][] superParameterAnnotations(Method method) {
        Class<?>[] parents = method.getDeclaringClass().getInterfaces();
        for (Class<?> p : parents) {
            Method superMethod = null;
            try {
                superMethod = p.getMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                continue;
            }

            return superMethod.getParameterAnnotations();
        }

        return null;
    }

    private static final Logger logger = LoggerFactory.getLogger(ElinkMonitor.class);
}

