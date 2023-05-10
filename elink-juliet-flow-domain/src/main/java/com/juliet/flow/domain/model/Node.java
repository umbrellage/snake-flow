package com.juliet.flow.domain.model;

import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.common.enums.NodeTypeEnum;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Data
public class Node extends BaseModel {

    private Long id;

    private List<Node> pre;

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
}
