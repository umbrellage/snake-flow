package com.juliet.flow.client;

import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.dto.*;
import com.juliet.flow.client.vo.NodeVO;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
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
    AjaxResult forward(@RequestBody FlowIdDTO dto);

    /**
     * 判断当前流程是否已经结束
     */
    @PostMapping("/is/end")
    AjaxResult flowIsEnd(@RequestBody FlowIdDTO dto);

    /**
     * 发起一个新的流程
     * @param dto 流程id
     * @return 流程实例id
     */
    AjaxResult<Long> initBmp(BpmDTO dto);

    /**
     * 获取当前所在的节点
     * @param dto
     * @return
     */
    AjaxResult<List<NodeVO>> currentNodeList(FlowIdDTO dto);

    AjaxResult<Void> claimTask(UserIdDTO dto);

}
