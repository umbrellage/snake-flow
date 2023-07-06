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
@FeignClient(name = "juliet-flow-callback", url = "${flow.callback.url:127.0.0.1:9400}")
public interface CallbackClient {

    @PostMapping("/juliet/flow/callback/notify")
    AjaxResult<Void> callback(@RequestBody List<NotifyDTO> dto);
}