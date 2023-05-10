package com.juliet.flow.controller;

import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.domain.dto.FlowTemplateAddDTO;
import com.juliet.flow.service.FlowTemplateService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Api(tags = "流程模板管理")
@RequestMapping("/juliet/flow/template")
@RestController
public class FlowTemplateController {

    @Autowired
    private FlowTemplateService flowTemplateService;

    @PostMapping("/add")
    public AjaxResult add(@RequestBody FlowTemplateAddDTO dto) {
        flowTemplateService.add(dto);
        return AjaxResult.success();
    }

    @PostMapping("/update")
    public AjaxResult update(@RequestBody FlowTemplateAddDTO dto) {
        flowTemplateService.update(dto);
        return AjaxResult.success();
    }

    @PostMapping("/disable")
    public AjaxResult disable(@RequestBody String body) {
        return AjaxResult.success();
    }
}
