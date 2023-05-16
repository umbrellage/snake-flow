package com.juliet.flow.client.aspect;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.JulietFlowClient;
import com.juliet.flow.client.callback.ControllerResponseCallback;
import com.juliet.flow.client.annotation.JulietFlowInterceptor;
import com.juliet.flow.client.callback.impl.DefaultControllerResponseCallbackImpl;
import com.juliet.flow.client.dto.FlowIdDTO;
import com.juliet.flow.client.utils.ServletUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

        List<String> fields = parseRequestParams(point);
        log.info("all fields:{}", fields);

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
            FlowIdDTO flowIdDTO = new FlowIdDTO();
            flowIdDTO.setFlowId(longJulietFlowId);

//            String bodyStr = ServletUtils.readBody(request);
//            Map<String, ?> map = JSON.parseObject(bodyStr, Map.class);
//            AjaxResult<Long> ajaxResult = julietFlowClient.forward(flowIdDTO, map);
//            if (ajaxResult.getCode().intValue() != 200) {
//                log.error("data saved but flow error:{}", ajaxResult);
//                throw new RuntimeException("数据已保存，但是流程异常:" + JSON.toJSONString(ajaxResult));
//            }
//            request.setAttribute("flowId", ajaxResult.getData());
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
        FlowIdDTO flowIdDTO = new FlowIdDTO();
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

    private Map<String, Object> getMethodArgs(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        // 参数名数组
        String[] parameterNames = ((MethodSignature) signature).getParameterNames();
        // 构造参数组集合
        Map<String, Object> map = new HashMap<>();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof HttpServletRequest || args[i] instanceof HttpServletResponse) {
                continue;
            }
            map.put(parameterNames[i], JSON.toJSON(args[i]));
        }
        return map;
    }

    private List<String> parseRequestParams(JoinPoint joinPoint) {
        Map<String, Object> map = getMethodArgs(joinPoint);
        if (CollectionUtils.isEmpty(map)) {
            return Lists.newArrayList();
        }
        List<String> fields = new ArrayList<>();
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(map));
        parseFieldByJsonObject(fields, jsonObject, "");
        // 去掉field的前缀
        List<String> formattedFields = new ArrayList<>();
        if (!CollectionUtils.isEmpty(fields)) {
            fields.forEach(field -> {
                int index = field.indexOf(".");
                if (index > 0) {
                    formattedFields.add(field.substring(index + 1));
                }
            });
        }
        return formattedFields;
    }

    private void parseFieldByJsonObject(List<String> fields, JSONObject jsonObject, String fieldPrefix) {
        if (jsonObject == null) {
            return;
        }
        for (String key : jsonObject.keySet()) {
            String wholeKey = key;
            if (!StringUtil.isEmpty(fieldPrefix)) {
                wholeKey = fieldPrefix + "." + key;
            }
            if (jsonObject.get(key) instanceof JSONObject) {
                parseFieldByJsonObject(fields, jsonObject.getJSONObject(key), wholeKey);
            } else if (jsonObject.get(key) instanceof JSONArray) {
                parseFieldByJsonArray(fields, jsonObject.getJSONArray(key), wholeKey);
            } else {
                fields.add(wholeKey);
            }
        }
    }

    private void parseFieldByJsonArray(List<String> fields, JSONArray jsonArray, String fieldPrefix) {
        if (jsonArray == null || jsonArray.size() == 0) {
            return;
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            parseFieldByJsonObject(fields, jsonArray.getJSONObject(i), fieldPrefix);
        }
    }
}
