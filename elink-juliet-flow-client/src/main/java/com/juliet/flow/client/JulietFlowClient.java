package com.juliet.flow.client;

import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.dto.*;
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

    @PostMapping("/forward")
    AjaxResult forward(@RequestBody FlowDTO dto);

    /**
     * 判断当前流程是否已经结束
     * @param dto flowId 必填
     */
    @PostMapping("/is/end")
    AjaxResult flowIsEnd(@RequestBody FlowDTO dto);

//    /**
//     * 发起一个新的流程
//     * @param dto flowId 必填
//     * @return 流程实例id
//     */
//    AjaxResult<Long> initBmp(BpmDTO dto);
//
//    /**
//     * 获取当前所在的节点
//     * @param dto flowId 必填
//     *
//     * @return
//     */
//    AjaxResult<List<NodeVO>> currentNodeList(FlowDTO dto);
//
//    /**
//     * 认领待办任务、修改待办人、分配一个待办人
//     * @param dto flowId 必填， nodeId必填
//     * @param userDTO userId 必填
//     * @return
//     */
//    AjaxResult<Void> claimTask(FlowDTO dto, UserDTO userDTO);
//
//    /**
//     * 执行一个节点任务
//     * @param dto 必填
//     * @param userDTO userId 必填
//     * @return
//     */
//    AjaxResult<Void> task(FlowDTO dto, UserDTO userDTO);
//
//
//    /**
//     * 获取所有的待办
//     * @param dto 必填
//     *
//     * @return
//     */
//    AjaxResult<List<NodeVO>> todoNodeList(UserDTO dto);

}
