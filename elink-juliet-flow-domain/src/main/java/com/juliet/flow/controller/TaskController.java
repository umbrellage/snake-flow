package com.juliet.flow.controller;

import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.dto.InvalidDTO;
import com.juliet.flow.client.dto.RollbackDTO;
import com.juliet.flow.service.FlowExecuteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TaskController
 *
 * @author Geweilang
 * @date 2023/8/4
 */
@Api(tags = "任务管理")
@RestController
@RequestMapping("/task")
public class TaskController {

    @ApiOperation("回退")
    @PostMapping("/bpm/rollback")
    public AjaxResult<Void> rollback(@RequestBody RollbackDTO dto) {
        flowExecuteService.execute(dto);
        return AjaxResult.success();
    }

    @ApiOperation("作废")
    @PostMapping("/bpm/invalid")
    public AjaxResult<Void> invalid(@RequestBody InvalidDTO dto) {
        flowExecuteService.invalid(dto);
        return AjaxResult.success();
    }

    @Autowired
    private FlowExecuteService flowExecuteService;
}
