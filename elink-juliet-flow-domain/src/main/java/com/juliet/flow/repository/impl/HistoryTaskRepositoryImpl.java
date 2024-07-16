package com.juliet.flow.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.juliet.flow.client.common.NodeStatusEnum;
import com.juliet.flow.client.dto.HistoricTaskQueryObject;
import com.juliet.flow.client.dto.HistoryTaskInstance;
import com.juliet.flow.dao.FlowDao;
import com.juliet.flow.dao.NodeDao;
import com.juliet.flow.domain.entity.FlowEntity;
import com.juliet.flow.domain.entity.NodeEntity;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.HistoryTaskInstanceImpl;
import com.juliet.flow.repository.HistoryTaskRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

/**
 * HistoryTaskRepositoryImpl
 *
 * @author Geweilang
 * @date 2024/7/13
 */
@RequiredArgsConstructor
@Repository
public class HistoryTaskRepositoryImpl implements HistoryTaskRepository {

    @Override
    public List<HistoryTaskInstance> list(HistoricTaskQueryObject queryObject) {

        List<Long> flowIdList = flowDao.selectList(Wrappers.<FlowEntity>lambdaQuery()
                .select(FlowEntity::getId)
                .eq(FlowEntity::getFlowTemplateId, queryObject.getTaskBpmId()))
            .stream()
            .map(FlowEntity::getId)
            .collect(Collectors.toList());

        List<NodeEntity> nodeEntityList = nodeDao.selectList(Wrappers.<NodeEntity>lambdaQuery()
            .in(CollectionUtils.isNotEmpty(queryObject.getTaskAssignees()), NodeEntity::getProcessedBy, queryObject.getTaskAssignees())
            .eq(queryObject.getFinished() != null && queryObject.getFinished(), NodeEntity::getStatus, NodeStatusEnum.PROCESSED.getCode())
            .le(queryObject.getFinishedBefore() != null, NodeEntity::getFinishTime, queryObject.getFinishedBefore())
            .ge(queryObject.getFinishedAfter() != null, NodeEntity::getFinishTime, queryObject.getFinishedAfter())
            .eq(queryObject.getTaskAssignee() != null, NodeEntity::getProcessedBy, queryObject.getTaskAssignee())
            .in(queryObject.getTaskBpmId() != null && CollectionUtils.isNotEmpty(flowIdList), NodeEntity::getFlowId, flowIdList)
            .eq(queryObject.getProcessInstanceId() != null, NodeEntity::getFlowId, queryObject.getProcessInstanceId())
            .in(CollectionUtils.isNotEmpty(queryObject.getProcessInstanceIds()), NodeEntity::getFlowId, queryObject.getProcessInstanceIds())
        );

        return nodeEntityList.stream()
            .map(node -> {
                HistoryTaskInstanceImpl instance = new HistoryTaskInstanceImpl();
                instance.setId(node.getId());
                instance.setTaskClaimTime(node.getClaimTime());
                instance.setTaskCreateTime(node.getActiveTime());
                instance.setTaskEndTime(node.getFinishTime());
                instance.setTaskAssignee(node.getProcessedBy());
                return instance;
            }).collect(Collectors.toList());
    }



    private final NodeDao nodeDao;
    private final FlowDao flowDao;
}
