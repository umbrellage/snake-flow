package com.juliet.flow.client;

import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.dto.*;
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
     * @return
     */
    AjaxResult<Void> initBmp(BpmDto dto);

}
