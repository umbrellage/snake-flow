package com.juliet.flow.client;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.dto.BpmDTO;
import java.util.function.Function;
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

    private static JulietFlowClient julietFlowClient;

    public static void setClient(JulietFlowClient client, BpmDTO bpmDTO) {
        julietFlowClient = client;
        BPM_DTO_CACHE.set(bpmDTO);
    }
    public static void putAttachment(String key, Object value) {
        Map<String, Object> local = LOCAL_CACHE.get();
        if (local == null) {
            local = new HashMap<>();
        }
        local.put(key, value);
        LOCAL_CACHE.set(local);
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

    public static Long submit(String templateCode, Long userId, Long tenantId) {
        return submit(templateCode, userId, tenantId, null, julietFlowClient::initBmp);
    }

    public static Long submitOnlyFlow(String templateCode, Long userId, Long tenantId) {
        return submit(templateCode, userId, tenantId, null, julietFlowClient::initBmpOnlyFlow);
    }

}
