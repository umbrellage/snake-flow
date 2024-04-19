package com.juliet.flow.client;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.juliet.common.core.exception.ServiceException;
import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.dto.BpmDTO;

import com.juliet.flow.client.dto.RedoDTO;
import com.juliet.flow.client.dto.RollbackDTO;
import java.util.List;
import java.util.function.Function;

import com.juliet.flow.client.dto.HistoricTaskInstance;
import com.juliet.flow.client.dto.NodeFieldDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xujianjie
 * @date 2023-07-25
 */
public class FlowContext {

    private static final Logger log = LoggerFactory.getLogger(FlowContext.class);

    private static TransmittableThreadLocal<Map<String, Object>> LOCAL_CACHE = new TransmittableThreadLocal<>();

    private static TransmittableThreadLocal<BpmDTO> BPM_DTO_CACHE = new TransmittableThreadLocal<>();

    private static TransmittableThreadLocal<NodeFieldDTO> NODE_FIELD_DTO_CACHE = new TransmittableThreadLocal<>();

    private static JulietFlowClient julietFlowClient;

    public static void setClient(JulietFlowClient client, BpmDTO bpmDTO) {
        julietFlowClient = client;
        BPM_DTO_CACHE.set(bpmDTO);
    }

    public static void setClient(JulietFlowClient client, NodeFieldDTO nodeFieldDTO) {
        julietFlowClient = client;
        NODE_FIELD_DTO_CACHE.set(nodeFieldDTO);
    }
    public static void putAttachment(String key, Object value) {
        Map<String, Object> local = LOCAL_CACHE.get();
        if (local == null) {
            local = new HashMap<>();
        }
        local.put(key, value);
        LOCAL_CACHE.set(local);
    }

    public static void putAutoOperator(Long userId) {
        putAttachment("autoOperator", userId);
    }

    public static Object getAttachment(String key) {
        Map<String, Object> local = LOCAL_CACHE.get();
        if (local != null) {
            return local.get(key);
        }
        return null;
    }

    public static Map<String, Object> getAttachmentMap() {
        return LOCAL_CACHE.get();
    }

    public static void clean() {
        LOCAL_CACHE.set(new HashMap<>());
    }

    public static Long submit() {
        try {
            BpmDTO bpmDTO = BPM_DTO_CACHE.get();
            bpmDTO.setData(LOCAL_CACHE.get());
            AjaxResult<Long> initResult = julietFlowClient.initBmp(bpmDTO);
            if (initResult == null || initResult.getCode() == null || initResult.getCode() != 200) {
                log.error("juliet flow init error! response:{}", initResult);
                throw new RuntimeException("juliet flow init error!");
            }
            return initResult.getData();
        } finally {
            clean();
        }
    }

    public static Long submit(String flowTemplateCode) {
        try {
            BpmDTO bpmDTO = BPM_DTO_CACHE.get();
            bpmDTO.setData(LOCAL_CACHE.get());
            bpmDTO.setTemplateCode(flowTemplateCode);
            AjaxResult<Long> initResult = julietFlowClient.initBmp(bpmDTO);
            if (initResult == null || initResult.getCode() == null || initResult.getCode() != 200) {
                log.error("juliet flow init error! response:{}", initResult);
                throw new RuntimeException("juliet flow init error!");
            }
            return initResult.getData();
        } finally {
            clean();
        }
    }

    public static Long submit(String templateCode, Long userId, Long tenantId, Map<String, Object> data, Function<BpmDTO, AjaxResult<Long>> function) {
        try {
            BpmDTO bpmDTO = new BpmDTO();
            bpmDTO.setTemplateCode(templateCode);
            bpmDTO.setUserId(userId);
            bpmDTO.setTenantId(tenantId);
            if (data != null && data.size() > 0) {
                bpmDTO.setData(data);
            }
            AjaxResult<Long> initResult = function.apply(bpmDTO);
            if (initResult == null || initResult.getCode() == null || initResult.getCode() != 200) {
                log.error("juliet flow init error! response:{}", initResult);
                throw new RuntimeException("juliet flow init error!");
            }
            return initResult.getData();
        } finally {
            clean();
        }
    }

    public static Long redo() {
        try {
            NodeFieldDTO nodeFieldDTO = NODE_FIELD_DTO_CACHE.get();
            nodeFieldDTO.setData(LOCAL_CACHE.get());
            RedoDTO redo = new RedoDTO();
            redo.setFlowId(nodeFieldDTO.getFlowId());
            redo.setUserId(nodeFieldDTO.getUserId());
            redo.setParam(nodeFieldDTO.getData());

            AjaxResult<List<HistoricTaskInstance>> initResult = julietFlowClient.bpmRedo(redo);
            if (initResult == null || initResult.getCode() == null || initResult.getCode() != 200) {
                log.error("juliet flow redo error! response:{}", initResult);
                throw new RuntimeException("juliet flow redo error!");
            }
            if (CollectionUtils.isEmpty(initResult.getData())) {
                throw new ServiceException("操作记录不存在，请检查");
            }
            return initResult.getData().get(0).flowId();
        } finally {
            clean();
        }
    }

    public static void flowAutomate(Long flowId, Map<String, Object> automateParam) {
        julietFlowClient.flowAutomate(flowId, automateParam);
    }

    public static List<HistoricTaskInstance> forward() {
        try {
            NodeFieldDTO nodeFieldDTO = NODE_FIELD_DTO_CACHE.get();
            nodeFieldDTO.setData(LOCAL_CACHE.get());
            AjaxResult<List<HistoricTaskInstance>> result = julietFlowClient.forwardV2(nodeFieldDTO);
            if (result == null || result.getCode() == null || result.getCode() != 200) {
                log.error("juliet flow forward error! response:{}", result);
                throw new RuntimeException("juliet flow forward error!");
            }
            return result.getData();
        } finally {
            clean();
        }
    }


    public static List<HistoricTaskInstance> forward(Long processedBy) {
        if (processedBy == null || processedBy == 0L) {
            throw new ServiceException("实际操作人不可以为空，如果实际操作人就是节点操作人的话请调用无参forward方法");
        }
        try {
            NodeFieldDTO nodeFieldDTO = NODE_FIELD_DTO_CACHE.get();
            Map<String, Object> data = LOCAL_CACHE.get();
            if (data == null) {
                data = new HashMap<>();
            }
            data.put("actualOperator", processedBy);
            nodeFieldDTO.setData(LOCAL_CACHE.get());
            AjaxResult<List<HistoricTaskInstance>> result = julietFlowClient.forwardV2(nodeFieldDTO);
            if (result == null || result.getCode() == null || result.getCode() != 200) {
                log.error("juliet flow forward error! response:{}", result);
                throw new RuntimeException("juliet flow forward error!");
            }
            return result.getData();
        } finally {
            clean();
        }
    }

    public static void tryForward() {
        NodeFieldDTO nodeFieldDTO = NODE_FIELD_DTO_CACHE.get();
        if (nodeFieldDTO.getFlowId() == null){
            throw new ServiceException("flowId不能为空");
        }
        if (nodeFieldDTO.getNodeId() == null) {
            throw new ServiceException("nodeId不能为空");
        }
        if (nodeFieldDTO.getUserId() == null) {
            throw new ServiceException("userId不能为空");
        }
    }

    public static void rollbackStart(String reason) {
        NodeFieldDTO nodeFieldDTO = NODE_FIELD_DTO_CACHE.get();
        if (nodeFieldDTO.getNodeId() == null || nodeFieldDTO.getFlowId() == null || nodeFieldDTO.getUserId() == null) {
            throw new ServiceException("参数不正确");
        }
        RollbackDTO dto = new RollbackDTO();
        dto.setUserId(nodeFieldDTO.getUserId());
        dto.setReason(reason);
        dto.setFlowId(String.valueOf(nodeFieldDTO.getFlowId()));
        dto.setNodeId(String.valueOf(nodeFieldDTO.getNodeId()));
        dto.setRollbackType(0);
        julietFlowClient.rollback(dto);
    }

    public static Long submitOnlyFlow() {
        try {
            BpmDTO bpmDTO = BPM_DTO_CACHE.get();
            bpmDTO.setData(LOCAL_CACHE.get());
            AjaxResult<Long> initResult = julietFlowClient.initBmpOnlyFlow(bpmDTO);
            if (initResult == null || initResult.getCode() == null || initResult.getCode() != 200) {
                log.error("juliet flow init error! response:{}", initResult);
                throw new RuntimeException("juliet flow init error!");
            }
            return initResult.getData();
        } finally {
            clean();
        }
    }

    public static Long submit(String templateCode, Long userId, Long tenantId) {
        return submit(templateCode, userId, tenantId, null, julietFlowClient::initBmp);
    }

    public static Long submitOnlyFlow(String templateCode, Long userId, Long tenantId) {
        return submit(templateCode, userId, tenantId, null, julietFlowClient::initBmpOnlyFlow);
    }

}
