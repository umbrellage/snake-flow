package com.juliet.flow.client.dto;

import com.juliet.flow.client.common.OperateTypeEnum;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * RedoDTO
 *
 * @author Geweilang
 * @date 2023/9/19
 */
@Data
public class RedoDTO implements TaskExecute{

    @NotNull
    private Long flowId;
    private Long nodeId;
    @NotNull
    private Long userId;
    @NotNull
    private Map<String, Object> param;

    @Override
    public OperateTypeEnum getTaskType() {
        return OperateTypeEnum.REDO;
    }
}
