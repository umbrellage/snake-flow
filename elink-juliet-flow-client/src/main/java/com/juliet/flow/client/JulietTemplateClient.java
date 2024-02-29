package com.juliet.flow.client;

import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.dto.FlowTemplateAddDTO;
import com.juliet.flow.client.vo.NodeVO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * JulietTemplateClient
 *
 * @author Geweilang
 * @date 2024/1/17
 */
@FeignClient(contextId = "JulietTemplateClient" , name = "elink-juliet-flow", path = "/juliet/flow/template")
public interface JulietTemplateClient {

    @PostMapping("/add")
    AjaxResult<Long> add(@RequestBody FlowTemplateAddDTO dto);
    @PostMapping("/update")
    AjaxResult update(@RequestBody FlowTemplateAddDTO dto);
    @GetMapping("/nodeList")
    AjaxResult<List<NodeVO>> nodeList(@RequestParam("id") Long id);
    @GetMapping("/detailByCode")
    AjaxResult detailByCode(@RequestParam("code") String code);

}
