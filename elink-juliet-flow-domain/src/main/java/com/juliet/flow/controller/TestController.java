package com.juliet.flow.controller;

import com.juliet.common.core.web.domain.AjaxResult;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xujianjie
 * @date 2023-04-24
 */
@Api(tags = "商品管理")
@RequestMapping("/item")
@RestController
public class TestController {

    @GetMapping("/test")
    public AjaxResult moduleDelete() {
        return AjaxResult.success();
    }
}
