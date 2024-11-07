package com.juliet.flow.controller;

import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.domain.dto.FlowFormFieldsUpdateDTO;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.service.FlowManagerService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xujianjie
 * @date 2023-05-17
 */
@Api(tags = "流程管理")
@RequestMapping("/juliet/flow")
@RestController
public class FlowController {

    @Autowired
    private FlowRepository flowRepository;

    @Autowired
    private FlowManagerService flowManagerService;

    @GetMapping("/detail")
    public AjaxResult detail(@RequestParam("id") Long id) {
        return AjaxResult.success(flowRepository.queryById(id));
    }

    @GetMapping("/graph")
    public AjaxResult graph(@RequestParam("id") Long id) {
        return AjaxResult.success(flowManagerService.getGraph(id));
    }

    @PostMapping("/form/fields/update")
    public AjaxResult<Void> updateFlowFormFields(@RequestBody FlowFormFieldsUpdateDTO dto) {
        flowManagerService.updateFlowFormFields(dto);
        return AjaxResult.success();
    }
}
