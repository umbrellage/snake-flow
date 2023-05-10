package com.juliet.flow.client.aspect;

import com.alibaba.fastjson2.JSON;
import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.JulietFlowClient;
import com.juliet.flow.client.callback.ControllerResponseCallback;
import com.juliet.flow.client.annotation.JulietFlowInterceptor;
import com.juliet.flow.client.callback.impl.DefaultControllerResponseCallbackImpl;
import com.juliet.flow.client.dto.FlowDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Aspect
@Component
@Slf4j
public class FlowAspect {

    @Autowired
    private JulietFlowClient julietFlowClient;

    @Autowired
    private List<ControllerResponseCallback> callbacks;

    @Pointcut("@annotation(com.juliet.flow.client.annotation.JulietFlowInterceptor)")
    public void pointcut() {
    }

    @Around("pointcut()&&@annotation(julietFlowInterceptor)")
    public Object doAround(ProceedingJoinPoint point, JulietFlowInterceptor julietFlowInterceptor) throws Throwable {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        if (null == sra) {
            throw new RuntimeException("please put the JulietFlowSubmit on the controller method");
        }
        HttpServletRequest request = sra.getRequest();
        String julietFlowId = request.getParameter("julietFlowId");
        if (julietFlowId == null || julietFlowId.length() == 0) {
            throw new RuntimeException("By use annotation of JulietFlowSubmit, Required request parameter 'julietFlowId'");
        }
        Long longJulietFlowId;
        try {
            longJulietFlowId = Long.parseLong(julietFlowId);
        } catch (Exception e) {
            log.error("julietFlowId type must be Long!", e);
            throw new RuntimeException("julietFlowId type must be Long! but " + julietFlowId);
        }

        // 判断一下当前流程是否已经结束了
        Boolean end = getCurrentFlowIsEnd(longJulietFlowId);
        if (end == null) {
            throw new RuntimeException("get current flow status fail, flow id:" + longJulietFlowId);
        }
        if (end) {
            throw new RuntimeException("current flow is ended!, flow id:" + longJulietFlowId);
        }

        Object result = point.proceed();
        if (responseIsSuccess(result)) {
            log.info("juliet flow forward by flow id:{}", longJulietFlowId);
            FlowDTO flowIdDTO = new FlowDTO();
            flowIdDTO.setFlowId(longJulietFlowId);
            AjaxResult ajaxResult = julietFlowClient.forward(flowIdDTO);
            if (ajaxResult.getCode().intValue() != 200) {
                log.error("data saved but flow error:{}", ajaxResult);
                throw new RuntimeException("数据已保存，但是流程异常:" + JSON.toJSONString(ajaxResult));
            }
        } else {
            log.info("data saved error, flow abort! flow id:{}", longJulietFlowId);
        }
        return result;
    }

    private boolean responseIsSuccess(Object object) {
        // 只有juliet的一个默认实现
        if (callbacks.size() == 1) {
            return callbacks.get(0).responseIsSuccess(object);
        }
        for (ControllerResponseCallback callback : callbacks) {
            if (callback instanceof DefaultControllerResponseCallbackImpl) {
                continue;
            }
            return callback.responseIsSuccess(object);
        }
        throw new RuntimeException("impossible!");
    }

    private Boolean getCurrentFlowIsEnd(Long flowId) {
        FlowDTO flowIdDTO = new FlowDTO();
        flowIdDTO.setFlowId(flowId);
        AjaxResult<Boolean> ajaxResult = julietFlowClient.flowIsEnd(flowIdDTO);
        if (ajaxResult != null && ajaxResult.getCode() != null && ajaxResult.getCode().intValue() == 200) {
            if (ajaxResult.getData() != null) {
                return ajaxResult.getData();
            }
        } else {
            log.error("query current flow status error:{}", ajaxResult);
        }
        return null;
    }
}
