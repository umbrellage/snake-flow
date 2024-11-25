package com.juliet.flow.client.aspect;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.FlowContext;
import com.juliet.flow.client.JulietFlowClient;
import com.juliet.flow.client.callback.ControllerResponseCallback;
import com.juliet.flow.client.annotation.JulietFlowInterceptor;
import com.juliet.flow.client.callback.UserInfoCallback;
import com.juliet.flow.client.callback.impl.DefaultControllerResponseCallbackImpl;
import com.juliet.flow.client.common.FlowMode;
import com.juliet.flow.client.dto.BpmDTO;
import com.juliet.flow.client.dto.NodeFieldDTO;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
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
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class FlowAspect {

    @Autowired
    private JulietFlowClient julietFlowClient;

    @Autowired
    private List<ControllerResponseCallback> callbacks;

    @Autowired(required = false)
    private UserInfoCallback userInfoCallback;

    private static final String HEADER_NAME_JULIET_FLOW_CODE = "Juliet-Flow-Code";

    private static final String PARAM_NAME_JULIET_FLOW_CODE = "julietFlowCode";

    private static final String HEADER_NAME_JULIET_FLOW_ID = "Juliet-Flow-Id";

    private static final String HEADER_NAME_JULIET_NODE_ID = "Juliet-Node-Id";

    private static final String PARAM_NAME_JULIET_FLOW_ID = "julietFlowId";

    private static final String PARAM_NAME_JULIET_NODE_ID = "julietNodeId";

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

        String julietFlowCode = getJulietFlowCode(request);
//        String julietFlowCode = processConfigCallback.getProcessConfigId(request);

        Long julietFlowId = getJulietFlowId(request);
        Long julietNodeId = getJulietNodeId(request);

        List<String> fields = parseRequestParams(point);
        log.info("juliet flow interceptor all fields:{}", fields);

        if (userInfoCallback == null) {
            throw new RuntimeException("Juliet Flow UserInfoCallback 未实现!");
        }

        Long userId = userInfoCallback.getUserId(request);
        Long tenantId = userInfoCallback.getTenantId(request);

        boolean bpmInit = false;
        if (julietFlowId == null) {
            if (julietFlowCode == null || julietFlowCode.length() == 0) {
                throw new RuntimeException(
                    "By use annotation of JulietFlowInterceptor, Required request header 'juliet-flow-code' or parameter 'julietFlowCode'");
            }
            bpmInit = true;
            BpmDTO bpmDTO = toBpmDTO(julietFlowCode, userId, tenantId);
            log.info("bpmDTO:{}", JSON.toJSONString(bpmDTO));
            if (julietFlowInterceptor.flowMode() == FlowMode.AUTO) {
                julietFlowId = function(julietFlowClient::initBmp, bpmDTO);
            }
            if(julietFlowInterceptor.flowMode() == FlowMode.MANUAL) {
                FlowContext.setClient(julietFlowClient, bpmDTO);
            }
        } else {
//            FlowIdDTO id = new FlowIdDTO();
//            id.setFlowId(julietFlowId);
//            AjaxResult<FlowVO> flowResult = julietFlowClient.flow(id);
//            if (flowResult.getCode() != 200 || flowResult.getData() == null) {
//                throw new ServiceException("当前用户没有操作权限");
//            }
////            if (flowResult.getData().end()) {
////                throw new ServiceException("流程已经结束");
////            }
//            NodeVO node = flowResult.getData().getNodes().stream()
//                .filter(nodeVO -> nodeVO.getId().equals(julietNodeId))
//                .findAny()
//                .orElse(null);
//            if (flowResult.getData().getSubFlowCount() >= 10 && node != null && node.getStatus() == 4) {
//                throw new ServiceException("当前已存在10条子流程，目前只允许创建10条.");
//            }
//            TaskDTO dto = new TaskDTO();
//            dto.setFlowId(julietFlowId);
//            dto.setNodeId(julietNodeId);
//            AjaxResult<NodeVO> result = julietFlowClient.findNodeByFlowIdAndNodeId(dto);
//            if (result.getCode() != 200 || result.getData() == null) {
//                log.error("error data:{}", result);
//                throw new ServiceException(result.getMsg());
//            }
//            NodeVO nodeVO = result.getData();
//            if (!userId.equals(nodeVO.getProcessedBy())) {
//                throw new ServiceException("当前用户没有操作权限");
//            }
            log.info("juliet flow pre forward!");
            NodeFieldDTO nodeFieldDTO = new NodeFieldDTO();
            nodeFieldDTO.setFieldCodeList(fields);
            nodeFieldDTO.setUserId(userId);
            nodeFieldDTO.setFlowId(julietFlowId);
            nodeFieldDTO.setNodeId(julietNodeId);

            FlowContext.setClient(julietFlowClient, nodeFieldDTO);
        }

        request.setAttribute(PARAM_NAME_JULIET_FLOW_ID, julietFlowId);

        boolean businessForwardSuccess = false;
        try {
            Object result = point.proceed();
            if (responseIsSuccess(result)) {
                businessForwardSuccess = true;
            }
            return result;
        } catch (Exception e) {
            log.error("business forward exception!", e);
            throw e;
        } finally {
            if (julietFlowInterceptor.flowMode() == FlowMode.AUTO) {
                if (businessForwardSuccess) {
                    log.info("juliet flow forward by flow id:{}", julietFlowId);
                    // 业务处理成功，流程往后走
                    if (!bpmInit) {
                        NodeFieldDTO nodeFieldDTO = toNodeFieldDTO(fields, julietFlowId, julietNodeId, userId);
                        AjaxResult forwardResult = julietFlowClient.forward(nodeFieldDTO);
                        if (!isSuccess(forwardResult)) {
                            log.error("business forward success but flow error! flow id:{}, request:{}, response:{}",
                                    julietFlowId, JSON.toJSONString(nodeFieldDTO), JSON.toJSONString(forwardResult));
                        }
                        log.info("juliet flow forward success!");
                    }
                } else {
                    log.error("business forward error, flow abort! flow id:{}", julietFlowId);
                    if (bpmInit) {
                        // 初始化流程的情况下，业务处理失败，流程结束
                        // TODO 关闭流程
                    }
                }
            }
            FlowContext.clean();
        }
    }

    private BpmDTO toBpmDTO(String julietFlowCode, Long userId, Long tenantId) {
        BpmDTO bpmDTO = new BpmDTO();
        bpmDTO.setTemplateCode(julietFlowCode);
        bpmDTO.setData(FlowContext.getAttachmentMap() == null ? new HashMap<>() : FlowContext.getAttachmentMap());
        bpmDTO.setUserId(userId);
        bpmDTO.setTenantId(tenantId);
        return bpmDTO;
    }

    private NodeFieldDTO toNodeFieldDTO(List<String> fields, Long julietFlowId, Long julietNodeId, Long userId) {
        NodeFieldDTO nodeFieldDTO = new NodeFieldDTO();
        nodeFieldDTO.setFieldCodeList(fields);
        nodeFieldDTO.setData(FlowContext.getAttachmentMap() == null ? new HashMap<>() : FlowContext.getAttachmentMap());
        nodeFieldDTO.setNodeId(julietNodeId);
        nodeFieldDTO.setFlowId(julietFlowId);
        nodeFieldDTO.setUserId(userId);
        return nodeFieldDTO;
    }

    public Long function(Function<BpmDTO, AjaxResult<Long>> function, BpmDTO dto) {
        AjaxResult<Long> result = function.apply(dto);
        if (!isSuccess(result)) {
            log.error("juliet flow init error! julietFlowCode:{}, response:{}", dto.getTemplateCode(), result);
            throw new RuntimeException(result.getMsg());
        }
        Long julietFlowId = result.getData();
        if (julietFlowId == null) {
            log.error("juliet flow init error! julietFlowId is null! julietFlowCode:{}, response:{}",
                dto.getTemplateCode(), result);
            throw new RuntimeException("juliet flow init error! flow id is null!");
        }
        log.info("juliet flow init success!");
        return julietFlowId;
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
            if (jsonArray.get(i) instanceof JSONObject) {
                parseFieldByJsonObject(fields, jsonArray.getJSONObject(i), fieldPrefix);
            }
        }
    }

    private String getJulietFlowCode(HttpServletRequest request) {
        String julietFlowCode = null;
        julietFlowCode = request.getHeader(HEADER_NAME_JULIET_FLOW_CODE);
        if (julietFlowCode != null) {
            return julietFlowCode;
        }
        julietFlowCode = request.getParameter(PARAM_NAME_JULIET_FLOW_CODE);
        return julietFlowCode;
    }

    private boolean isSuccess(AjaxResult ajaxResult) {
        return ajaxResult != null && ajaxResult.getCode() != null && ajaxResult.getCode() == 200;
    }

    private Long getJulietFlowId(HttpServletRequest request) {
        String julietFlowId = null;
        julietFlowId = request.getHeader(HEADER_NAME_JULIET_FLOW_ID);
        if (julietFlowId == null || julietFlowId.trim().length() == 0) {
            julietFlowId = request.getParameter(PARAM_NAME_JULIET_FLOW_ID);
        }
        if (julietFlowId == null || julietFlowId.trim().length() == 0) {
            return null;
        }
        try {
            Long longJulietFlowId = Long.parseLong(julietFlowId);
            if (longJulietFlowId <= 0) {
                throw new RuntimeException("invalid juliet flow id, value:" + longJulietFlowId);
            }
            return longJulietFlowId;
        } catch (Exception e) {
            log.error("julietFlowId type must be Long!", e);
            throw new RuntimeException("request parameter `julietFlowId` type must be Long! but " + julietFlowId);
        }
    }

    private Long getJulietNodeId(HttpServletRequest request) {
        String julietFlowNodeId = null;
        julietFlowNodeId = request.getHeader(HEADER_NAME_JULIET_NODE_ID);
        if (julietFlowNodeId == null || julietFlowNodeId.trim().length() == 0) {
            julietFlowNodeId = request.getParameter(PARAM_NAME_JULIET_NODE_ID);
        }
        if (julietFlowNodeId == null || julietFlowNodeId.trim().length() == 0) {
            return null;
        }
        if ("null".equals(julietFlowNodeId)) {
            return null;
        }
        try {
            Long longJulietFlowNodeId = Long.parseLong(julietFlowNodeId);
            if (longJulietFlowNodeId <= 0) {
                throw new RuntimeException("invalid juliet flow node id, value:" + longJulietFlowNodeId);
            }
            return longJulietFlowNodeId;
        } catch (Exception e) {
            log.error("julietFlowNodeId type must be Long!", e);
            throw new RuntimeException(
                "request parameter `julietFlowNodeId` type must be Long! but " + julietFlowNodeId);
        }
    }

//    private boolean isCurrentProcessor(Long flowId) {
//        FlowIdDTO flowIdDTO = new FlowIdDTO();
//        flowIdDTO.setFlowId(flowId);
//        AjaxResult<FlowVO> result = julietFlowClient.flow(flowIdDTO);
//        if (result != null) {
//            if (result.getCode() != null && result.getCode().intValue() == 200 && result.getData() != null) {
//            }
//        }
//    }
}
