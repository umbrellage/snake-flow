package com.juliet.flow.callback.impl;

import com.alibaba.fastjson2.JSON;
import com.juliet.flow.callback.MsgNotifyCallback;
import com.juliet.flow.client.CallbackClient;
import com.juliet.flow.client.callback.NotifyMessageDTO;
import com.juliet.flow.client.dto.NotifyDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * DefaultNotifyCallback
 *
 * @author Geweilang
 * @date 2023/5/23
 */
@Service
@Slf4j
public class DefaultNotifyCallback implements MsgNotifyCallback {

    @Autowired
    private CallbackClient callbackClient;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Value("${rocketmq.producer.topic}")
    private String flowNotifyTopic;

    @Override
    public void notify(List<NotifyDTO> list) {
//        log.info("notify param:{}", JSON.toJSONString(list));
//        try {
//            AjaxResult<Void> result = callbackClient.callback(list.stream()
//                .filter(e -> e.getType() != NotifyTypeEnum.END && e.getType() != NotifyTypeEnum.INVALID)
//                .collect(Collectors.toList())
//            );
//            log.info("callback result:{}", result);
//        } catch (Exception e) {
//            log.error("callback error!", e);
//        }
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
        dto.setMsg(notifyDTO.getRemark());
        dto.setUserId(notifyDTO.getUserId());
        return dto;
    }
}
