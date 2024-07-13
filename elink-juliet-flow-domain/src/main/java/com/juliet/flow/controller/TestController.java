package com.juliet.flow.controller;

import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.HistoryTaskService;
import com.juliet.flow.client.dto.HistoricTaskQueryObject;
import com.juliet.flow.client.dto.HistoryTaskInstance;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.repository.impl.FlowCache;
import com.juliet.flow.service.FlowExecuteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xujianjie
 * @date 2023-04-24
 */
@Api(tags = "系统管理")
@RequestMapping("/juliet/flow/system")
@RestController
@Slf4j
public class TestController {

    @Autowired
    private FlowCache flowCache;

    @Autowired
    private FlowRepository flowRepository;

    @Autowired
    private FlowExecuteService flowExecuteService;
    @Autowired
    private HistoryTaskService historyTaskService;

    @ApiOperation("重置缓存")
    @GetMapping("/cache/reset")
    public AjaxResult<Void> resetCache() {
        flowCache.reset();
        return AjaxResult.success();
    }

    @ApiOperation("消息重发")
    @GetMapping("/msg/resend")
    public AjaxResult<Void> resendMsg(@RequestParam("flowId") Long flowId) {
        flowExecuteService.resetMsgByFlowId(flowId);
        return AjaxResult.success();
    }

    @ApiOperation("消息重发")
    @GetMapping("/trx/test")
    public AjaxResult<Void> trxTest() {
        flowExecuteService.trxTest();
        return AjaxResult.success();
    }

    @ApiOperation("消息重发")
    @GetMapping("/trx/test2")
    public AjaxResult<Void> trxTest2() {
        flowExecuteService.trxTest2();
        return AjaxResult.success();
    }


    @ApiOperation("消息重发")
    @PostMapping("/select/test3")
    public AjaxResult<List<HistoryTaskInstance>> selectTest3(@RequestBody HistoricTaskQueryObject query) {
        List<HistoryTaskInstance> list = historyTaskService.list(query);
        return AjaxResult.success(list);
    }
}
