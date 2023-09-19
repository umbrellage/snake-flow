package com.juliet.flow.client;

import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.dto.*;
import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.GraphVO;
import com.juliet.flow.client.vo.NodeVO;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author xujianjie
 * @date 2023-04-23
 */
@FeignClient(name = "elink-juliet-flow", path = "/juliet/flow/execute")
public interface JulietFlowClient {

    @PostMapping("/open")
    AjaxResult<NodeVO> open(@RequestBody FlowOpenDTO dto);

    @PostMapping("/bpm/forward")
    AjaxResult<Void> forward(@RequestBody NodeFieldDTO dto);

    @PostMapping("/bpm/forwardV2")
    AjaxResult<List<HistoricTaskInstance>> forwardV2(@RequestBody NodeFieldDTO dto);

    @PostMapping("/customer/status")
    AjaxResult<List<String>> customerStatus(@RequestBody FlowOpenDTO dto);

    /**
     * 根据字段列表查询节点
     * @param dto
     * @return
     */
    @PostMapping("/bpm/nodeField")
    AjaxResult<NodeVO> node(@RequestBody NodeFieldDTO dto);



    /**
     * 判断当前流程是否已经结束
     * @param dto flowId 必填
     */
    @PostMapping("/is/end")
    AjaxResult<Boolean> flowIsEnd(@RequestBody FlowIdDTO dto);

    /**
     * 发起一个新的流程
     * @param dto templateId 必填
     * @return 流程实例id
     */
    @PostMapping("/init/bpm")
    AjaxResult<Long> initBmp(@RequestBody BpmDTO dto);

    /**
     * 发起一个新的流程
     * @param dto templateId 必填
     * @return 流程实例id
     */
    @PostMapping("/init/bpmV2")
    AjaxResult<HistoricTaskInstance> initBmpV2(@RequestBody BpmDTO dto);

    /**
     * 发起一个新的流程
     * @param dto templateId 必填
     * @return 流程实例id
     */
    @PostMapping("/init/bpm/onlyFlow")
    AjaxResult<Long> initBmpOnlyFlow(@RequestBody BpmDTO dto);

    /**
     * 获取当前所在的节点
     * @param dto flowId 必填
     *
     * @return
     */
    @PostMapping("/current/node")
    AjaxResult<List<NodeVO>> currentNodeList(@RequestBody FlowIdDTO dto);

    /**
     * 认领待办任务、修改待办人、分配一个待办人
     * @param dto
     * @return
     */
    @PostMapping("/claim/task")
    AjaxResult<Void> claimTask(@RequestBody TaskDTO dto);

    /**
     * 获取节点
     * @param dto
     * @return
     */
    @PostMapping("/bpm/node")
    AjaxResult<NodeVO> node(@RequestBody TaskDTO dto);

    /**
     * 获取节点
     * @param dto
     * @return
     */
    @PostMapping("/bpm/new/node")
    AjaxResult<NodeVO> findNodeByFlowIdAndNodeId(@RequestBody TaskDTO dto);

    /**
     * 执行一个节点任务
     * @param dto 必填
     * @return
     */
    @Deprecated
    @PostMapping("/bpm/task")
    AjaxResult<List<HistoricTaskInstance>> task(@RequestBody TaskDTO dto);


    /**
     * 获取所有的待办
     * @param dto 必填
     *
     * @return
     */
    @PostMapping("/bpm/backlog")
    AjaxResult<List<NodeVO>> todoNodeList(@RequestBody UserDTO dto);

    /**
     * 获取所有的可办
     * @param dto 必填
     *
     * @return
     */
    @PostMapping("/bpm/canDo")
    AjaxResult<List<NodeVO>> canDoNodeList(@RequestBody UserDTO dto);

    /**
     * 获取流程信息
     * @param dto
     * @return
     */
    @PostMapping("/bpm/flow")
    AjaxResult<FlowVO> flow(@RequestBody FlowIdDTO dto);


    /**
     * 获取流程信息
     * @param dto
     * @return
     */
    @PostMapping("/bpm/flowList")
    AjaxResult<List<FlowVO>> flowList(@RequestBody FlowIdListDTO dto);


    @GetMapping("/graph")
    AjaxResult<GraphVO> graph(@RequestParam(value = "id", required = true) Long id, @RequestParam(value = "userId", required = false) Long userId);


    @GetMapping("/template/graph")
    AjaxResult<GraphVO> templateGraph(@RequestParam(value = "id", required = false) Long id,
                                      @RequestParam(value = "id", required = false, defaultValue = "flow_dev_process_ty") String code);

    /**
     * 拒绝
     * @param dto
     * @return
     */
    @PostMapping("/bpm/reject")
    AjaxResult<Void> reject(@RequestBody RejectDTO dto);

    @ApiOperation("作废")
    @PostMapping("/bpm/invalid")
    AjaxResult<Void> invalid(@RequestBody InvalidDTO dto);

    @ApiOperation("触发待办")
    @PostMapping("/bpm/trigger")
    AjaxResult<Void> triggerTodo(@RequestParam Long flowId,
                                 @RequestBody Map<String, Object> triggerParam);

}
