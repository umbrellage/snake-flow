package com.juliet.flow.controller;

import com.alibaba.fastjson2.JSON;
import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.JulietFlowClient;
import com.juliet.flow.client.dto.BpmDTO;
import com.juliet.flow.client.dto.FlowIdDTO;
import com.juliet.flow.client.dto.FlowIdListDTO;
import com.juliet.flow.client.dto.FlowOpenDTO;
import com.juliet.flow.client.dto.HistoricTaskInstance;
import com.juliet.flow.client.dto.InvalidDTO;
import com.juliet.flow.client.dto.NodeFieldDTO;
import com.juliet.flow.client.dto.RedoDTO;
import com.juliet.flow.client.dto.RejectDTO;
import com.juliet.flow.client.dto.TaskDTO;
import com.juliet.flow.client.dto.UserDTO;
import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.GraphVO;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.common.StatusCode;
import com.juliet.flow.common.utils.BusinessAssert;
import com.juliet.flow.client.dto.FieldDTO;
import com.juliet.flow.client.dto.FlowOpenResultDTO;
import com.juliet.flow.domain.model.Field;
import com.juliet.flow.domain.model.Node;
import com.juliet.flow.service.FlowExecuteService;
import com.juliet.flow.service.FlowManagerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Collections;
import java.util.List;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Slf4j
@Api(tags = "流程管理")
@RequestMapping("/juliet/flow/execute")
@RestController
public class FlowExecuteController implements JulietFlowClient {

    @Autowired
    private FlowExecuteService flowExecuteService;

    @Autowired
    private FlowManagerService flowManagerService;

    /**
     * 流程实例还没创建，做预创建时的查询
     */
    @ApiOperation("获取流程模版的开始节点")
    @Override
    public AjaxResult<NodeVO> open(FlowOpenDTO dto) {
        NodeVO node = flowExecuteService.queryStartNodeById(dto);
        BusinessAssert.assertNotNull(node, StatusCode.SERVICE_ERROR, "can not find node by code:" + dto.getCode());
        return AjaxResult.success(node);
    }

    @ApiOperation("通过表单字段查询节点，并执行")
    @Override
    public AjaxResult<Void> forward(NodeFieldDTO dto) {
        flowExecuteService.forward(dto);
        return AjaxResult.success();
    }

    @ApiOperation("通过表单字段查询节点，并执行")
    @Override
    public AjaxResult<List<HistoricTaskInstance>> forwardV2(NodeFieldDTO dto) {
        log.info("mamba param:{}", JSON.toJSONString(dto));
        List<HistoricTaskInstance> instanceList = flowExecuteService.forward(dto);
        return AjaxResult.success(instanceList);
    }

    @Override
    public AjaxResult<List<String>> customerStatus(FlowOpenDTO dto) {
        return AjaxResult.success(flowExecuteService.customerStatus(dto.getCode(), dto.getTenantId()));
    }

    @Override
    public AjaxResult<NodeVO> node(NodeFieldDTO dto) {
        NodeVO nodeVO = flowExecuteService.fieldNode(dto);
        return AjaxResult.success(nodeVO);
    }

    @ApiOperation("流程是否结束")
    @Override
    public AjaxResult<Boolean> flowIsEnd(@RequestBody FlowIdDTO dto) {
        return AjaxResult.success(flowExecuteService.flowEnd(dto.getFlowId()));
    }

    @ApiOperation("初始化一个流程")
    @Override
    public AjaxResult<Long> initBmp(BpmDTO dto) {
        Long flowId = flowExecuteService.startFlow(dto);
        return AjaxResult.success(flowId);
    }

    @Override
    public AjaxResult<HistoricTaskInstance> initBmpV2(BpmDTO dto) {
        return AjaxResult.success(flowExecuteService.startFlowV2(dto));
    }

    @Override
    public AjaxResult<Long> initBmpOnlyFlow(BpmDTO dto) {
        return AjaxResult.success(flowExecuteService.startOnlyFlow(dto));
    }

    @ApiOperation("获取当前流程所在的节点")
    @Override
    public AjaxResult<List<NodeVO>> currentNodeList(FlowIdDTO dto) {
        List<NodeVO> nodeVOList = flowExecuteService.currentNodeList(dto.getFlowId());
        return AjaxResult.success(nodeVOList);
    }

    @ApiOperation("认领待办任务、修改待办人、分配一个待办人")
    @Override
    public AjaxResult<Void> claimTask(TaskDTO dto) {
        flowExecuteService.claimTask(dto);
        return AjaxResult.success();
    }

    @ApiOperation("获取节点")
    @Override
    public AjaxResult<NodeVO> node(TaskDTO dto) {
        NodeVO nodeVO = flowExecuteService.node(dto);
        return AjaxResult.success(nodeVO);
    }

    @Override
    public AjaxResult<NodeVO> findNodeByFlowIdAndNodeId(TaskDTO dto) {
        return AjaxResult.success(flowExecuteService.findNodeByFlowIdAndNodeId(dto));
    }

    @ApiOperation("执行一个节点任务")
    @Override
    public AjaxResult<List<HistoricTaskInstance>> task(TaskDTO dto) {
        return AjaxResult.success();
    }

    @ApiOperation("获取所有的待办")
    @Override
    public AjaxResult<List<NodeVO>> todoNodeList(UserDTO dto) {
        List<NodeVO> nodeVOList = flowExecuteService.todoNodeList(dto);
        return AjaxResult.success(nodeVOList);
    }

    @ApiOperation("获取所有的可办")
    @Override
    public AjaxResult<List<NodeVO>> canDoNodeList(UserDTO dto) {
        List<NodeVO> nodeVOList = flowExecuteService.canDoNodeList(dto);
        return AjaxResult.success(nodeVOList);
    }

    @ApiOperation("获取流程")
    @Override
    public AjaxResult<FlowVO> flow(@RequestBody FlowIdDTO dto) {
        FlowVO flowVO = flowExecuteService.flow(dto.getFlowId());
        return AjaxResult.success(flowVO);
    }
    @ApiOperation("获取流程列表")
    @Override
    public AjaxResult<List<FlowVO>> flowList(FlowIdListDTO dto) {
        List<FlowVO> flowVOList = flowExecuteService.flowList(dto);
        return AjaxResult.success(flowVOList);
    }

    @ApiOperation("获取流程图")
    @Override
    public AjaxResult<GraphVO> graph(Long id, Long userId) {
        return AjaxResult.success(flowManagerService.getGraph(id, userId));
    }

    @Override
    public AjaxResult<GraphVO> templateGraph(Long id, String templateCode) {
         return AjaxResult.success(flowManagerService.getTemplateGraph(id, templateCode));
    }

    @Override
    public AjaxResult<Void> reject(RejectDTO dto) {
        flowExecuteService.execute(dto);
        return AjaxResult.success();
    }

    @Override
    public AjaxResult<List<HistoricTaskInstance>> bpmRedo(RedoDTO redo) {
        return AjaxResult.success(flowExecuteService.execute(redo));
    }

    @Override
    public AjaxResult<Void> invalid(InvalidDTO dto) {
        flowExecuteService.invalid(dto);
        return AjaxResult.success();
    }

    @Override
    public AjaxResult<Void> triggerTodo(Long flowId, Map<String, Object> triggerParam) {
        flowExecuteService.triggerTodo(flowId, triggerParam);
        return AjaxResult.success();
    }

    @Override
    public AjaxResult<Void> flowAutomate(Long flowId, Map<String, Object> automateParam) {
        flowExecuteService.flowAutomate(flowId, automateParam);
        return AjaxResult.success();
    }

    @Override
    public AjaxResult<Void> earlyEndFlow(Long flowId) {
        flowExecuteService.earlyEndFlow(flowId);
        return AjaxResult.success();
    }

    @Override
    public AjaxResult<Void> recoverFlow(FlowIdDTO flowId) {

        flowExecuteService.recoverFlow(flowId);
        return AjaxResult.success();
    }

    @Override
    public AjaxResult<Void> endFlowRollback(FlowIdDTO flowId, Integer level) {
        flowExecuteService.endFlowRollback(flowId, level);
        return AjaxResult.success();
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
