package com.juliet.flow.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.juliet.flow.client.dto.HistoricTaskQueryObject;
import com.juliet.flow.client.dto.HistoryTaskInstance;
import com.juliet.flow.dao.NodeDao;
import com.juliet.flow.domain.entity.NodeEntity;
import com.juliet.flow.domain.model.HistoryTaskInstanceImpl;
import com.juliet.flow.repository.HistoryTaskRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
        List<NodeEntity> nodeEntityList = nodeDao.selectList(Wrappers.<NodeEntity>lambdaQuery()
            .eq(queryObject.getTaskAssignee() != null, NodeEntity::getProcessedBy, queryObject.getTaskAssignee())
            .eq(queryObject.getTaskBpmId() != null, NodeEntity::getFlowTemplateId, queryObject.getTaskBpmId())
        );
        List<HistoryTaskInstanceImpl> instanceList = nodeEntityList.stream()
            .map(node -> {
                HistoryTaskInstanceImpl instance = new HistoryTaskInstanceImpl();
                instance.setId(node.getId());
                instance.setTaskClaimTime(node.getClaimTime());
                instance.setTaskCreateTime(node.getActiveTime());
                instance.setTaskEndTime(node.getFinishTime());
                return instance;
            })
            .collect(Collectors.toList());

        return new ArrayList<>(instanceList);
    }



    private final NodeDao nodeDao;
}
