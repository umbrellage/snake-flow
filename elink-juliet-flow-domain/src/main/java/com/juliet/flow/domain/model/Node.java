package com.juliet.flow.domain.model;

import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.client.vo.PostVO;
import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.common.enums.NodeTypeEnum;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Data
public class Node extends BaseModel {

    private Long id;

    private String name;

    private Long preNodeId;

    private Long nextNodeId;

    private List<Node> next;

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
     * 处理人
     */
    private Long processedBy;

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

    public NodeVO toNodeVo(Long flowId) {
        NodeVO data = new NodeVO();
        data.setId(id);
        data.setFlowId(flowId);
        data.setForm(form.toForm());
        data.setProcessedBy(processedBy);
        if (CollectionUtils.isNotEmpty(bindPosts)) {
            List<PostVO> postVOList = bindPosts.stream()
                .map(Post::toPost)
                .collect(Collectors.toList());
            data.setBindPosts(postVOList);
        }
        return data;
    }
}
