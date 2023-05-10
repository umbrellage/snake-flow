package com.juliet.flow.controller;

import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.annotation.JulietFlowInterceptor;
import com.juliet.flow.domain.dto.FlowManagerCreateDTO;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Api(tags = "流程执行")
@RequestMapping("/juliet/flow/manager")
@RestController
public class FlowManageController {

    @PostMapping("/create")
    public AjaxResult open(@RequestBody FlowManagerCreateDTO dto) {
        return AjaxResult.success();
    }

    @PostMapping("/disable")
    public AjaxResult forward(@RequestParam("julietProcessId") String processId,
                           @RequestBody String body) {
        System.out.println(processId);
        System.out.println(body);
        return AjaxResult.success();
    }
}
