package com.juliet.flow.callback.impl;

import com.alibaba.fastjson2.JSON;
import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.callback.MsgNotifyCallback;
import com.juliet.flow.client.CallbackClient;
import com.juliet.flow.client.callback.NotifyMessageDTO;
import com.juliet.flow.client.common.NotifyTypeEnum;
import com.juliet.flow.client.dto.NotifyDTO;

import java.util.List;

import java.util.stream.Collectors;
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

    @Value(("${spring.rabbitmq.exchange.callback}"))
    private String exchange;

    @Autowired
    private AmqpTemplate rabbitMqTemplate;
    @Autowired
    private CallbackClient callbackClient;

    @Override
    public void notify(List<NotifyDTO> list) {
        log.info("notify param:{}", JSON.toJSONString(list));
        try {
            AjaxResult<Void> result = callbackClient.callback(list.stream()
                .filter(e -> e.getType() != NotifyTypeEnum.END && e.getType() != NotifyTypeEnum.INVALID)
                .collect(Collectors.toList())
            );
            log.info("callback result:{}", result);
        } catch (Exception e) {
            log.error("callback error!", e);
        }
    }

    @Override
    public void message(List<NotifyDTO> list) {
        try {
            for (NotifyDTO notifyDTO : list) {
                NotifyMessageDTO dto = toMessageDTO(notifyDTO);
                rabbitMqTemplate.convertAndSend(exchange, "default", JSON.toJSONString(dto));
            }
        } catch (Exception e) {
            log.error("send callback msg to mq fail!", e);
        }
    }


    private NotifyMessageDTO toMessageDTO(NotifyDTO notifyDTO) {
        NotifyMessageDTO dto = new NotifyMessageDTO();
        dto.setFlowId(notifyDTO.getFlowId());
        dto.setTemplateCode(notifyDTO.getCode());
        dto.setType(notifyDTO.getType());
        dto.setTenantId(notifyDTO.getTenantId());
        return dto;
    }
}
