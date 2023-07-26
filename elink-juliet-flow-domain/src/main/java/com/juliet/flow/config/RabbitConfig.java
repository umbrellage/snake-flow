package com.juliet.flow.config;

import com.juliet.flow.client.config.FastJsonMessageConverter;
import java.text.MessageFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xujianjie
 * @date 2023-07-20
 */
@Configuration
@Slf4j
public class RabbitConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
//        template.setMessageConverter(new Jackson2JsonMessageConverter());
        template.setMessageConverter(new FastJsonMessageConverter());
        template.setConfirmCallback((data, ack, cause) -> {
            String msgId = data.getId();
            if (ack) {
                log.info(msgId + ": 消息发送成功");
            } else {
                log.info(msgId + ": 消息发送失败");
            }
        });

        template.setReturnsCallback((message) -> {

            log.info(MessageFormat.format("消息发送失败，ReturnCallback:{0}", message));


        });
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
//        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setMessageConverter(new FastJsonMessageConverter());
        return factory;
    }
}
