package com.juliet.flow.controller;

import com.alibaba.fastjson2.JSON;
import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.callback.MsgNotifyCallback;
import com.juliet.flow.client.dto.NotifyDTO;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.Node;
import com.juliet.flow.domain.model.NodeQuery;
import com.juliet.flow.repository.FlowRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
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
@Api(tags = "商品管理")
@RequestMapping("/item")
@RestController
@Slf4j
public class TestController {

    @Autowired
    FlowRepository flowRepository;
    @Autowired
    private List<MsgNotifyCallback> msgNotifyCallbacks;

    @ApiOperation("获取流程列表")
    @PostMapping("/test")
    public AjaxResult moduleDelete(HttpServletRequest request, @RequestBody NodeQuery query1) {
        return AjaxResult.success(flowRepository.listNode(query1));
    }


    @ApiOperation("回调消息")
    @PostMapping("/send")
    public AjaxResult send() {
        Flow flow = flowRepository.queryById(1134501098053963776L);
        flow.modifyNextNodeStatus(1134501098104295424L, Collections.emptyMap());
        log.info(JSON.toJSONString(flow));
        return AjaxResult.success();
    }
}
