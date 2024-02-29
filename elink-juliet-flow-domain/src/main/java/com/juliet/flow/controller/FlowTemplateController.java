package com.juliet.flow.controller;

import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.JulietTemplateClient;
import com.juliet.flow.client.dto.FieldDTO;
import com.juliet.flow.client.dto.NodeDTO;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.common.StatusCode;
import com.juliet.flow.common.utils.BusinessAssert;
import com.juliet.flow.client.dto.FlowTemplateAddDTO;
import com.juliet.flow.domain.model.FlowTemplate;
import com.juliet.flow.service.FlowTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Api(tags = "流程模板管理")
@RequestMapping("/juliet/flow/template")
@RestController
public class FlowTemplateController implements JulietTemplateClient {

    @Autowired
    private FlowTemplateService flowTemplateService;


    @ApiOperation("添加模版")
    @PostMapping("/add")
    public AjaxResult add(@RequestBody FlowTemplateAddDTO dto) {
        Long flowTemplateId = flowTemplateService.add(dto);
        return AjaxResult.success(flowTemplateId);
    }

    @Override
    public AjaxResult<List<NodeVO>> nodeList(Long id) {
        List<NodeVO> nodeVOList = flowTemplateService.nodeList(id);
        return AjaxResult.success(nodeVOList);
    }

    @ApiOperation("修改模版")
    @PostMapping("/update")
    public AjaxResult update(@RequestBody FlowTemplateAddDTO dto) {
        flowTemplateService.update(dto);
        return AjaxResult.success();
    }

    @GetMapping("/detail")
    public AjaxResult detail(@RequestParam("id") Long id) {
        return AjaxResult.success(flowTemplateService.queryById(id));
    }


    public AjaxResult<String> updateTimeByCode(String code) {
        return AjaxResult.success(flowTemplateService.updateTimeByCode(code));
    }

    @PostMapping("/disable")
    public AjaxResult disable(@RequestBody String body) {
        return AjaxResult.success();
    }

    private void checkNodeParams(NodeDTO nodeDTO) {
        if (nodeDTO.getForm() != null) {
            BusinessAssert.assertNotNull(nodeDTO.getForm().getName(),
                    StatusCode.ILLEGAL_PARAMS, "form.name");
            BusinessAssert.assertNotNull(nodeDTO.getForm().getCode(),
                    StatusCode.ILLEGAL_PARAMS, "form.code");
            BusinessAssert.assertNotNull(nodeDTO.getForm().getPath(),
                    StatusCode.ILLEGAL_PARAMS, "form.path");
            if (!CollectionUtils.isEmpty(nodeDTO.getForm().getFields())) {
                for (FieldDTO fieldDTO : nodeDTO.getForm().getFields()) {
                    if (fieldDTO != null) {
                        BusinessAssert.assertNotNull(fieldDTO.getCode(),
                                StatusCode.ILLEGAL_PARAMS, "form.field.code");
                        BusinessAssert.assertNotNull(fieldDTO.getName(),
                                StatusCode.ILLEGAL_PARAMS, "form.field.name");
                    }
                }
            }
        }
    }
}
