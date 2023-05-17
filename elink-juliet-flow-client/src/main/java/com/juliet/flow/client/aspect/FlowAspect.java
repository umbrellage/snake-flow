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
import com.juliet.flow.client.dto.BpmDTO;
import com.juliet.flow.client.dto.FlowIdDTO;
import com.juliet.flow.client.dto.NodeFieldDTO;
import com.juliet.flow.client.utils.ServletUtils;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.aspectj.apache.bcel.generic.RET;
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

    private static final String HEADER_NAME_JULIET_FLOW_CODE = "juliet-flow-code";

    private static final String PARAM_MAME_JULIET_FLOW_CODE = "julietFlowCode";

    private static final String PARAM_MAME_JULIET_FLOW_ID = "julietFlowId";

    @Pointcut("@annotation(com.juliet.flow.client.annotation.JulietFlowInterceptor)")
    public void pointcut() {
    }

    @Around("pointcut()&&@annotation(julietFlowInterceptor)")
    public Object doAround(ProceedingJoinPoint point, JulietFlowInterceptor julietFlowInterceptor) throws Throwable {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        if (null == sra) {
            throw new RuntimeException("please put the JulietFlowInterceptor on the controller method");
        }
        HttpServletRequest request = sra.getRequest();
        String julietFlowId = request.getParameter(PARAM_MAME_JULIET_FLOW_ID);
        String julietFlowCode = getJulietFlowCode(request);

        List<String> fields = parseRequestParams(point);
        log.info("juliet flow interceptor all fields:{}", fields);

        boolean bpmInit = true;
        Long longJulietFlowId = 0L;
        if (julietFlowId != null) {
            bpmInit = false;
            try {
                longJulietFlowId = Long.parseLong(julietFlowId);
                if (longJulietFlowId <= 0) {
                    throw new RuntimeException("invalid juliet flow id, value:" + longJulietFlowId);
                }
            } catch (Exception e) {
                log.error("julietFlowId type must be Long!", e);
                throw new RuntimeException("request parameter `julietFlowId` type must be Long! but " + julietFlowId);
            }
            AjaxResult<Long> initResult = julietFlowClient.forward(toNodeFieldDTO(fields, longJulietFlowId), julietFlowCode);
            if (!isSuccess(initResult)) {
                log.error("juliet flow init error! response:{}", initResult);
                throw new RuntimeException("juliet flow init error!");
            }
            longJulietFlowId = initResult.getData();
            request.getParameterMap().put(PARAM_MAME_JULIET_FLOW_ID, new String[] {String.valueOf(longJulietFlowId)});
        } else if (julietFlowCode == null || julietFlowCode.length() == 0) {
            throw new RuntimeException("By use annotation of JulietFlowInterceptor, Required request header 'juliet-flow-code' or parameter 'julietFlowCode'");
        }

        Object result = point.proceed();
        if (responseIsSuccess(result)) {
            log.info("juliet flow forward by flow id:{}", longJulietFlowId);
            // 业务处理成功，流程往后走
        } else {
            log.info("data saved error, flow abort! flow id:{}", longJulietFlowId);
            if (bpmInit) {
                // 初始化流程的情况下，业务处理失败，流程结束

            }
        }
        return result;
    }

    private NodeFieldDTO toNodeFieldDTO(List<String> fields, Long julietFlowId) {
        NodeFieldDTO nodeFieldDTO = new NodeFieldDTO();
        nodeFieldDTO.setFieldCodeList(fields);
        nodeFieldDTO.setFlowId(julietFlowId);
        return nodeFieldDTO;
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
        Set<String> formattedFields = new HashSet<>();
        if (!CollectionUtils.isEmpty(fields)) {
            fields.forEach(field -> {
                int index = field.indexOf(".");
                if (index > 0) {
                    formattedFields.add(field.substring(index + 1));
                }
            });
        }
        return formattedFields.stream().collect(Collectors.toList());
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
                if (jsonObject.get(key) != null) {
                    fields.add(wholeKey);
                }
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

    private String getJulietFlowCode(HttpServletRequest request) {
        String julietFlowCode = null;
        julietFlowCode = request.getHeader(HEADER_NAME_JULIET_FLOW_CODE);
        if (julietFlowCode != null) {
            return julietFlowCode;
        }
        julietFlowCode = request.getParameter(PARAM_MAME_JULIET_FLOW_CODE);
        return julietFlowCode;
    }

    private boolean isSuccess(AjaxResult ajaxResult) {
        return ajaxResult != null && ajaxResult.getCode() != null && ajaxResult.getCode().intValue() == 200;
    }
}
