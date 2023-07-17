package com.juliet.flow.client.config;

import feign.slf4j.Slf4jLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lg
 * @create 2023/6/5
 * @description ''
 */
@Configuration
public class FeiClientConfig {

    @Bean
    public feign.Logger logger() {
        return new Slf4jLogger();
    }

}
