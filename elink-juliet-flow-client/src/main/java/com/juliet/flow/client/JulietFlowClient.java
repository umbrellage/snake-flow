package com.juliet.flow.client;

import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.dto.*;
import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.NodeVO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
     * 执行一个节点任务
     * @param dto 必填
     * @return
     */
    @PostMapping("/bpm/task")
    AjaxResult<Void> task(@RequestBody TaskDTO dto);


    /**
     * 获取所有的待办
     * @param dto 必填
     *
     * @return
     */
    @PostMapping("/bpm/backlog")
    AjaxResult<List<NodeVO>> todoNodeList(@RequestBody UserDTO dto);

    /**
     * 获取流程信息
     * @param dto
     * @return
     */
    @PostMapping("/bpm/flow")
    AjaxResult<FlowVO> flow(FlowIdDTO dto);

}
