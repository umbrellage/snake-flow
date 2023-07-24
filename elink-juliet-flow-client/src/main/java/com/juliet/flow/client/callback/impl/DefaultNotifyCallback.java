package com.juliet.flow.client.callback.impl;

import com.alibaba.fastjson2.JSON;
import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.CallbackClient;
import com.juliet.flow.client.callback.MsgNotifyCallback;
import com.juliet.flow.client.callback.NotifyMessageDTO;
import com.juliet.flow.client.dto.NotifyDTO;

import java.io.IOException;
import java.util.List;

import com.juliet.flow.client.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * DefaultNotifyCallback
 *
 * @author Geweilang
 * @date 2023/5/23
 */
@Service
@Slf4j
public class DefaultNotifyCallback implements MsgNotifyCallback {
    @Value("${flow.callback.url:http://127.0.0.1:9400/todo/callback}")
//    @Value("${flow.callback.url:http://172.16.1.157:9400/todo/callback}")
    private String url;

//    @Value(("${spring.rabbitmq.exchange.callback}"))
//    private String exchange;

    @Autowired
    private AmqpTemplate rabbitMqTemplate;

    @Override
    public void notify(List<NotifyDTO> list) {
        log.info("notify param:{}, url{}", JSON.toJSONString(list), url);
        try {
            AjaxResult<Void> result = callbackClient.callback(list);
            log.info("callback result:{}", result);
        } catch (Exception e) {
            log.error("callback error!", e);
        }
    }

    public static void main(String[] args) throws IOException {
        HttpUtil.postJson("http://172.16.1.157:9400/todo/callback", "[]");
    }

    @Autowired
    private CallbackClient callbackClient;

    private NotifyMessageDTO toMessageDTO(NotifyDTO notifyDTO) {
        NotifyMessageDTO dto = new NotifyMessageDTO();
        dto.setFlowId(notifyDTO.getFlowId());
        return dto;
    }
}
