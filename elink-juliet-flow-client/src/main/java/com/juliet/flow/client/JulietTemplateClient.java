package com.juliet.flow.client;

import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.dto.FlowTemplateAddDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * JulietTemplateClient
 *
 * @author Geweilang
 * @date 2024/1/17
 */
@FeignClient(name = "elink-juliet-flow", path = "/juliet/flow/template")
public interface JulietTemplateClient {

    @PostMapping("/add")
    AjaxResult add(@RequestBody FlowTemplateAddDTO dto);
}
