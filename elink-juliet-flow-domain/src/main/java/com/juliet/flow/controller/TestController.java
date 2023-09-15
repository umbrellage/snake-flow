package com.juliet.flow.controller;

import com.alibaba.fastjson2.JSON;
import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.callback.impl.RabbitMqConfirmCallback;
import com.juliet.flow.client.dto.NotifyDTO;
import com.juliet.flow.domain.model.NodeQuery;
import com.juliet.flow.repository.FlowRepository;
import com.rabbitmq.client.Channel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xujianjie
 * @date 2023-04-24
 */
@Api(tags = "商品管理")
@RequestMapping("/item")
@RestController
@Slf4j
public class TestController {

    @Autowired
    FlowRepository flowRepository;

    @Autowired
    private AmqpTemplate rabbitMqTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RabbitMqConfirmCallback confirmCallback;

    @ApiOperation("获取流程列表")
    @PostMapping("/test")
    public AjaxResult moduleDelete(HttpServletRequest request, @RequestBody NodeQuery query1) {
        return AjaxResult.success(flowRepository.listNode(query1));
    }


    @ApiOperation("回调消息")
    @PostMapping("/send")
    public AjaxResult send() {
        NotifyDTO dto = new NotifyDTO();
        dto.setCode("dd");
//        rabbitTemplate.convertAndSend("juliet.test.exchange", "default", JSON.toJSONString(dto));
        log.info("data:{}", JSON.toJSONString(dto));
        return AjaxResult.success();
    }

//    @RabbitListener(queues = "juliet_test_queue")
//    public void consumer(String data, Channel channel, Message message) {
//        long tag = message.getMessageProperties().getDeliveryTag();
//        try {
//            channel.basicAck(tag, false);
//            channel.basicReject(tag, true);
//        } catch (IOException e) {
//            log.info("msg:{}", data);
//        }
//    }

}
