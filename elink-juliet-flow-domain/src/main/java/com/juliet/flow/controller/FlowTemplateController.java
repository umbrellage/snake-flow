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

    @Deprecated
    @ApiOperation("获取更新时间")
    public AjaxResult<String> updateTimeByCode(String code) {
        String data = flowTemplateService.updateTimeByCode(code);
        AjaxResult<String> result = AjaxResult.success();
        result.setData(data);
        return result;
    }

    @PostMapping("/disable")
    public AjaxResult disable(@RequestBody String body) {
        return AjaxResult.success();
    }

}
