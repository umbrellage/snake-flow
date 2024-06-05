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
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * DefaultNotifyCallback
 *
 * @author Geweilang
 * @date 2023/5/23
 */
@Service
@Slf4j
public class DefaultNotifyCallback implements MsgNotifyCallback {

//    @Value(("${spring.rabbitmq.exchange.callback}"))
//    private String exchange;

//    @Autowired
//    private AmqpTemplate rabbitMqTemplate;
    @Autowired
    private CallbackClient callbackClient;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Value("${rocketmq.producer.topic}")
    private String flowNotifyTopic;

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
        log.info("mq message:{}", JSON.toJSONString(list));
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        try {
            NotifyMessageDTO dto = toMessageDTO(list.get(0));
            log.info("transfer data:{}", JSON.toJSONString(dto));
//            rabbitMqTemplate.convertAndSend(exchange, "default", JSON.toJSONString(dto));
//            rocketMQTemplate.syncSend(flowNotifyTopic, dto);
            rocketMQTemplate.syncSendDelayTimeMills(flowNotifyTopic, dto, 500);
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
