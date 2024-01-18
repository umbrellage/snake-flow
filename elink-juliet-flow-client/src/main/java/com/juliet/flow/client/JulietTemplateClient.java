package com.juliet.flow.client;

import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.dto.FlowTemplateAddDTO;
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
@FeignClient(name = "julietTemplateClient", path = "/juliet/flow/template")
public interface JulietTemplateClient {

    @PostMapping("/add")
    AjaxResult<Long> add(@RequestBody FlowTemplateAddDTO dto);

    @GetMapping("/detail")
    AjaxResult detail(@RequestParam("id") Long id);
}
