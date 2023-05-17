package com.juliet.flow.controller;

import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.domain.model.NodeQuery;
import com.juliet.flow.repository.FlowRepository;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * @author xujianjie
 * @date 2023-04-24
 */
@Api(tags = "商品管理")
@RequestMapping("/item")
@RestController
public class TestController {

    @Autowired
    FlowRepository flowRepository;

    @GetMapping("/test")
    public AjaxResult moduleDelete() {
        NodeQuery query = new NodeQuery();
        query.setPostIds(Arrays.asList("a111222"));
        query.setUserId(1L);
        query.setTenantId(2L);
        return AjaxResult.success(flowRepository.listNode(query));
    }
}
