package com.juliet.flow.callback.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * @author xujianjie
 * @date 2023-08-01
 */
@Component
@Slf4j
public class RabbitMqConfirmCallback implements RabbitTemplate.ConfirmCallback {
    @Override
    public void confirm(CorrelationData data, boolean ack, String s) {
        String msgId = data.getId();
        if (ack) {
            log.info(msgId + ": 消息发送成功");
        } else {
            log.info(msgId + ": 消息发送失败");
        }
    }
}
