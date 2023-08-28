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
    private Boolean willEdit;
    private Boolean canChange;
    private Boolean adjustOperator;
    private Boolean currentOperator;// 当前操作人
}
