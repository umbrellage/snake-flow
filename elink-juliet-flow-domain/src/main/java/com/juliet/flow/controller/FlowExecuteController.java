package com.juliet.flow.controller;

import com.juliet.api.development.domain.entity.SysUser;
import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.common.security.utils.SecurityUtils;
import com.juliet.flow.client.JulietFlowClient;
import com.juliet.flow.client.dto.BpmDTO;
import com.juliet.flow.client.dto.FlowIdDTO;
import com.juliet.flow.client.dto.FlowOpenDTO;
import com.juliet.flow.client.dto.TaskDTO;
import com.juliet.flow.client.dto.UserDTO;
import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.common.StatusCode;
import com.juliet.flow.common.utils.BusinessAssert;
import com.juliet.flow.client.dto.FieldDTO;
import com.juliet.flow.client.dto.FlowOpenResultDTO;
import com.juliet.flow.domain.model.Field;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.Node;
import com.juliet.flow.service.FlowExecuteService;
import io.swagger.annotations.Api;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Api(tags = "流程管理")
@RequestMapping("/juliet/flow/execute")
@RestController
public class FlowExecuteController implements JulietFlowClient {

    @Autowired
    private FlowExecuteService flowExecuteService;

    /**
     * 流程实例还没创建，做预创建时的查询
     */
    @Override
    public AjaxResult<NodeVO> open(FlowOpenDTO dto) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        Long tenantId = sysUser.getTenantId();
        Long userId = sysUser.getUserId();
        NodeVO node = flowExecuteService.queryStartNodeByCode(tenantId, dto.getCode());
        BusinessAssert.assertNotNull(node, StatusCode.SERVICE_ERROR, "can not find node by code:" + dto.getCode());
        return AjaxResult.success(node);
    }

    @Override
    public AjaxResult<Void> forward(FlowIdDTO dto, Map<String, ?> map) {
        flowExecuteService.forward(dto.getFlowId(), map);
        return AjaxResult.success();
    }

    @Override
    public AjaxResult<Boolean> flowIsEnd(@RequestBody FlowIdDTO dto) {
        return AjaxResult.success(new Flow().isEnd());
    }

    @Override
    public AjaxResult<Long> initBmp(BpmDTO dto) {
        Long flowId = flowExecuteService.startFlow(dto.getTemplateId());
        return AjaxResult.success(flowId);
    }

    @Override
    public AjaxResult<List<NodeVO>> currentNodeList(FlowIdDTO dto) {
        List<NodeVO> nodeVOList = flowExecuteService.currentNodeList(dto.getFlowId());
        return AjaxResult.success(nodeVOList);
    }

    @Override
    public AjaxResult<Void> claimTask(TaskDTO dto) {
        flowExecuteService.claimTask(dto.getFlowId(), dto.getNodeId(), dto.getUserId());
        return AjaxResult.success();
    }

    @Override
    public AjaxResult<Void> task(TaskDTO dto) {
        flowExecuteService.task(dto.getFlowId(), dto.getNodeId(), dto.getNodeName(), dto.getUserId());
        return AjaxResult.success();
    }

    @Override
    public AjaxResult<List<NodeVO>> todoNodeList(UserDTO dto) {
        List<NodeVO> nodeVOList = flowExecuteService.todoNodeList(dto.getUserId());
        return AjaxResult.success(nodeVOList);
    }

    @Override
    public AjaxResult<FlowVO> flow(FlowIdDTO dto) {
        FlowVO flowVO = flowExecuteService.flow(dto.getFlowId());
        return AjaxResult.success(flowVO);
    }

    private static FlowOpenResultDTO toFlowOpenResultDTO(Node node) {
        FlowOpenResultDTO dto = new FlowOpenResultDTO();
        if (node.getForm() != null && !CollectionUtils.isEmpty(node.getForm().getFields())) {
            dto.setAllowFields(node.getForm().getFields().stream().map(FlowExecuteController::toFieldDTO)
                .collect(Collectors.toList()));
        }
        return dto;
    }

    private static FieldDTO toFieldDTO(Field field) {
        FieldDTO fieldDTO = new FieldDTO();
        fieldDTO.setName(field.getName());
        fieldDTO.setCode(field.getCode());
        return fieldDTO;
    }

}
