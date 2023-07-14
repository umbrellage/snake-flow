package com.juliet.flow.client;

import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.dto.NotifyDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * CallbackClient
 *
 * @author Geweilang
 * @date 2023/7/4
 */
@FeignClient(name = "juliet-flow-callback")
public interface CallbackClient {

    @PostMapping("/todo/callback")
    AjaxResult<Void> callback(@RequestBody List<NotifyDTO> dto);
}
