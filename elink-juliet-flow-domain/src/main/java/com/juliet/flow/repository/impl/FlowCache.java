package com.juliet.flow.repository.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.juliet.common.redis.service.RedisService;
import com.juliet.flow.domain.entity.FlowEntity;
import com.juliet.flow.domain.entity.NodeEntity;
import com.juliet.flow.domain.model.Field;
import com.juliet.flow.domain.model.Flow;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author xujianjie
 * @date 2024-03-14
 */
@Component
public class FlowCache {

    private static final String FLOW_CACHE_PREFIX = "flow_id_";

    private static final String NODE_CACHE_PREFIX = "node_id_";

    private static final String FLOW_NODE_CACHE_PREFIX = "flow_node_id_";

    private static final String FORM_FIELDS_CACHE_PREFIX = "form_fields:";

//    private static Cache<String, Flow> cache = CacheBuilder.newBuilder()
//            .maximumSize(1000)
//            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
//            .expireAfterWrite(10, TimeUnit.DAYS)
//            .build();

    private static Cache<String, Flow> parentFlowCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            .expireAfterWrite(10, TimeUnit.DAYS)
            .build();

    @Autowired
    private RedisService redisService;

    public void setFlow(Flow flow) {
        if (flow == null || flow.getId() == null) {
            return;
        }
        String key = buildFlowCacheKey(flow.getId());
//        cache.put(key, flow);
        redisService.<Flow>setCacheObject(key, flow, 10L, TimeUnit.MINUTES);
    }
    public void setFlowList(List<Flow> flowList) {
        for (Flow flow : flowList) {
            setFlow(flow);
        }
    }

    public Flow getFlow(Long id) {
//        return cache.getIfPresent(buildFlowCacheKey(id));
        return redisService.<Flow>getCacheObject(buildFlowCacheKey(id));
    }

    public void removeFlow(Long id) {
        redisService.deleteObject(buildFlowCacheKey(id));
//        cache.invalidate(buildFlowCacheKey(id));
    }

    public FlowCacheData getFlowList(List<Long> idList) {
        FlowCacheData flowCacheData = new FlowCacheData();
        List<Long> missKeyList = new ArrayList<>();
        List<Flow> flowList = new ArrayList<>();
        for (Long id : idList) {
            Flow flow = getFlow(id);
            if (flow != null) {
                flowList.add(flow);
            } else {
                missKeyList.add(id);
            }
        }
        flowCacheData.setFlowList(flowList);
        flowCacheData.setMissKeyList(missKeyList);
        return flowCacheData;
    }

    public void reset() {
//        cache.invalidateAll();
    }

    private String buildFlowCacheKey(FlowEntity flowEntity) {
        return buildFlowCacheKey((flowEntity.getId()));
    }

    private String buildFlowCacheKey(Long flowId) {
        return FLOW_CACHE_PREFIX + flowId;
    }

    private String buildNodeCacheKey(NodeEntity nodeEntity) {
        return buildNodeCacheKey((nodeEntity.getId()));
    }

    private String buildNodeCacheKey(Long nodeId) {
        return NODE_CACHE_PREFIX + nodeId;
    }

    private String buildFlowNodeCacheKey(Long flowId) {
        return FLOW_NODE_CACHE_PREFIX + flowId;
    }

    public void setFormFields(Long formId, List<Field> fields) {
        redisService.setCacheList(buildFormFieldsKey(formId), fields);
    }

    public List<Field> getFormFields(Long formId) {
        return redisService.getCacheList(buildFormFieldsKey(formId));
    }

    @Data
    public static class FlowCacheData {

        List<Long> missKeyList;

        List<Flow> flowList;
    }

    private String buildFormFieldsKey(Long id) {
        return FORM_FIELDS_CACHE_PREFIX + id;
    }
}
