package com.juliet.flow.client.dto;

import com.juliet.flow.client.dto.NodeDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import org.bouncycastle.util.Longs;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
public class FlowTemplateAddDTO {

    private Long id;

    private String name;

    private String code;

    @NotNull
    private List<NodeDTO> nodes;

    private Long createBy;

    private Long updateBy;

    private Long tenantId;

    private ProcessConfigRPCDTO dto;

}
