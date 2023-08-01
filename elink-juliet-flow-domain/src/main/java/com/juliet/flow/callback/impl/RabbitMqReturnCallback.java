package com.juliet.flow.callback.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

/**
 * @author xujianjie
 * @date 2023-08-01
 */
@Component
@Slf4j
public class RabbitMqReturnCallback implements RabbitTemplate.ReturnsCallback {
    @Override
    public void returnedMessage(ReturnedMessage message) {
        log.info(MessageFormat.format("消息发送失败，ReturnCallback:{0}", message));
    }
}
