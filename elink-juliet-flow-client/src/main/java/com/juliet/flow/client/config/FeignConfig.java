package com.juliet.flow.client.config;

import feign.slf4j.Slf4jLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FeignConfig
 *
 * @author Geweilang
 * @date 2023/7/17
 */
@Configuration
public class FeignConfig {

    @Bean
    public feign.Logger logger() {
        return new Slf4jLogger();
    }
}
