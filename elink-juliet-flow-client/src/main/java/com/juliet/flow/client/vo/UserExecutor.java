package com.juliet.flow.client.vo;

import lombok.Data;

/**
 * UserExecutor
 *
 * @author Geweilang
 * @date 2023/8/25
 */
@Data
public class UserExecutor {

    private Boolean canEdit;
    // 未来会操作的节点
    private Boolean willEdit;
    // 存在已操作过的节点会需要重复再操作的情况，该节点是到操作人的
    private Boolean willReEdit;
    private Boolean canChange;
    private Boolean adjustOperator;
    private Boolean currentOperator;// 当前操作人
}
