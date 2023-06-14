package com.juliet.flow.domain.model;

import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.client.common.NotifyTypeEnum;
import com.juliet.flow.client.dto.NotifyDTO;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.client.vo.PostVO;
import com.juliet.flow.client.vo.ProcessedByVO;
import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.common.enums.NodeTypeEnum;
import com.juliet.flow.common.utils.IdGenerator;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Data
public class Node extends BaseModel {

    private Long id;

    private Long flowId;

    private String title;

    private String name;

    private String preName;

    private String nextName;

    /**
     * 表单
     */
    private Form form;

    private NodeStatusEnum status;

    private NodeTypeEnum type;

    private List<Post> bindPosts;
    /**
     * 准入规则
     */
    private BaseRule accessRule;

    /**
     * 提交规则
     */
    private BaseRule submitRule;



    /**
     * 主管分配
     */
    private Boolean supervisorAssignment;

    /**
     * 认领+调整
     */
    private Boolean selfAndSupervisorAssignment;

    /**
     * 规则分配
     */
    private Boolean ruleAssignment;

    /**
     * 分配规则
     */
    private BaseAssignRule assignRule;

    /**
     * 主管ID列表
     */
    private List<Long> supervisorIds;

    /**
     * 处理人
     */
    private Long processedBy;
    private LocalDateTime processedTime;

    public boolean complianceTheRules() {
        // TODO: 2023/5/23  
        return true;
    }


    public NotifyDTO toNotifyNormal(Flow flow) {
        NotifyDTO ret = new NotifyDTO();
        ret.setNodeId(id);
        ret.setNodeName(name);
        ret.setFlowId(flowId);
        ret.setPostIdList(postIdList());
        ret.setUserId(processedBy);
        ret.setMainFlowId(flow.getParentId());
        ret.setType(NotifyTypeEnum.NORMAL);
        ret.setTenantId(getTenantId());
        ret.setPreprocessedBy(processedByList(flow));
        return ret;
    }

    public NotifyDTO toNotifyAnomaly(Flow flow) {
        NotifyDTO ret = new NotifyDTO();
        ret.setNodeId(id);
        ret.setNodeName(name);
        ret.setFlowId(flowId);
        ret.setUserId(processedBy);
        ret.setPostIdList(postIdList());
        ret.setMainFlowId(flow.getParentId());
        ret.setType(NotifyTypeEnum.ANOMALY);
        ret.setTenantId(getTenantId());
        ret.setPreprocessedBy(processedByList(flow));
        return ret;
    }

    public List<String> postIdList() {
        return bindPosts.stream()
            .map(Post::getPostId)
            .collect(Collectors.toList());
    }

    /**
     * 判断该岗位是否有该节点权限
     * @param postIdList
     * @return
     */
    public boolean postAuthority(List<Long> postIdList) {
        if (CollectionUtils.isEmpty(bindPosts)) {
            throw new ServiceException("当前节点没有绑定权限");
        }
        List<Long> sourcePostIdList = bindPosts.stream()
            .map(Post::getPostId)
            .filter(Objects::nonNull)
            .map(Long::parseLong)
            .collect(Collectors.toList());

        return !Collections.disjoint(postIdList, sourcePostIdList);
    }

    /**
     * 判断节点是否已经存在处理人
     * @return
     */
    public boolean nodeTodo() {
        return processedBy != null && processedBy != 0;
    }


    /**
     * 获取前置节点列表
     *
     * @return
     */
    public List<String> preNameList() {
        if (StringUtils.isBlank(preName)) {
            return Collections.emptyList();
        }
        return Arrays.stream(preName.split(","))
            .collect(Collectors.toList());
    }

    /**
     * 获取后置节点列表
     *
     * @return
     */
    public List<String> nextNameList() {
        if (StringUtils.isBlank(nextName)) {
            return Collections.emptyList();
        }
        return Arrays.stream(nextName.split(","))
            .collect(Collectors.toList());
    }

    /**
     * 节点是否已处理
     *
     * @return
     */
    public boolean isProcessed() {
        return status == NodeStatusEnum.PROCESSED;
    }

    /**
     * 判断节点是否是一个可被执行的
     *
     * @return
     */
    public boolean isExecutable() {
        return status == NodeStatusEnum.PROCESSED || status == NodeStatusEnum.ACTIVE;
    }

    /**
     * 判断节点是否是一个可被正常执行的
     *
     * @return
     */
    public boolean isNormalExecutable() {
        return status == NodeStatusEnum.ACTIVE;
    }


    /**
     * 判断节点是否是待办的
     *
     * @return
     */
    public boolean isTodoNode() {
        return status == NodeStatusEnum.ACTIVE || status == NodeStatusEnum.TO_BE_CLAIMED;
    }


    /**
     * 通过岗位判断当前用户是否可以操作
     */
    public boolean isOperator(Long[] postIds) {
        if (ArrayUtils.isEmpty(postIds)) {
            return false;
        }
        if (CollectionUtils.isEmpty(bindPosts)) {
            return false;
        }
        for (Long postId : postIds) {
            if (bindPosts.stream().anyMatch(post -> post.getPostId().equals(String.valueOf(postId)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param flow 当前流程
     * @return
     */
    public NodeVO toNodeVo(Flow flow) {
        NodeVO data = new NodeVO();
        data.setId(id);
        data.setName(name);
        data.setTitle(title);
        data.setFlowId(flowId);
        data.setPreName(preName);
        data.setNextName(nextName);
        if (status != null) {
            data.setStatus(status.getCode());
        }
        Optional.ofNullable(form).ifPresent(form -> data.setForm(form.toForm()));
        data.setProcessedBy(processedBy);
        if (CollectionUtils.isNotEmpty(bindPosts)) {
            List<PostVO> postVOList = bindPosts.stream()
                .map(Post::toPost)
                .collect(Collectors.toList());
            data.setBindPosts(postVOList);
        }
        data.setProcessedTime(processedTime);

        if (flow != null) {
            data.setMainFlowId(flow.getParentId());
            List<ProcessedByVO> preProcessedBy = processedByList(flow);
            data.setPreprocessedBy(preProcessedBy);
        }
        data.setTenantId(getTenantId());

        return data;
    }

    public List<ProcessedByVO> processedByList(Flow flow) {
        return preNameList().stream()
            .map(flow::findNode)
            .filter(Objects::nonNull)
            .map(node -> ProcessedByVO.of(node.getProcessedBy(), node.getProcessedTime()))
            .collect(Collectors.toList());
    }

    public Node copyNode() {
        Node node = new Node();
        node.id = IdGenerator.getId();
        node.title = title;
        node.name = name;
        node.preName = preName;
        node.nextName = nextName;
        node.form = form;
        node.status = status;
        node.type = type;
        node.bindPosts = bindPosts;
        node.accessRule = accessRule;
        node.submitRule = submitRule;
        node.processedBy = processedBy;
        return node;
    }

}
