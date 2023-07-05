package com.juliet.flow.domain.model;

import com.juliet.flow.common.enums.NodeStatusEnum;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author xujianjie
 * @date 2023-05-17
 */
@Data
public class NodeQuery {

    private final static int DEFAULT_PAGE_SIZE = 1000;

    private final static int MAX_PAGE_SIZE = 1000;

    private Long userId;

    private Long supervisorId;

    private List<String> postIds;

    private Long tenantId;

    private List<Integer> statusList;

    private Integer pageNo;

    private Integer pageSize;

    public Integer getOffset() {
        if (pageNo == null) {
            pageNo = 1;
        }
        return (pageNo - 1) * getPageSize();
    }

    public Integer getPageSize() {
        if (pageSize == null) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
        return pageSize;
    }

    public static NodeQuery findByUserId(Long userId) {
        NodeQuery data = new NodeQuery();
        data.setUserId(userId);
        data.setStatusList(Stream.of(NodeStatusEnum.TO_BE_CLAIMED, NodeStatusEnum.ACTIVE).map(NodeStatusEnum::getCode)
            .collect(Collectors.toList()));
        return data;
    }

    public static NodeQuery findBySupervisorId(Long userId) {
        NodeQuery data = new NodeQuery();
        data.setSupervisorId(userId);
        data.setStatusList(Stream.of(NodeStatusEnum.TO_BE_CLAIMED).map(NodeStatusEnum::getCode)
            .collect(Collectors.toList()));
        return data;
    }



    public static NodeQuery findByPostId(List<Long> postIdList) {
        NodeQuery data = new NodeQuery();
        if (CollectionUtils.isNotEmpty(postIdList)) {
            data.setPostIds(postIdList.stream().map(String::valueOf).collect(Collectors.toList()));
        }
        data.setStatusList(Stream.of(NodeStatusEnum.TO_BE_CLAIMED, NodeStatusEnum.ACTIVE).map(NodeStatusEnum::getCode)
            .collect(Collectors.toList()));
        return data;
    }
}
