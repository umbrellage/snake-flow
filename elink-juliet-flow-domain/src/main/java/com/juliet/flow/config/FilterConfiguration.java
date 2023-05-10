package com.juliet.flow.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xujianjie
 * @date 2023-05-04
 */
@Configuration
public class FilterConfiguration {

    @Bean
    public FilterRegistrationBean<RpcGzipFilter> gzipFilter(){
        FilterRegistrationBean<RpcGzipFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RpcGzipFilter());
        registration.addUrlPatterns("/item/center/*");
        registration.setName("gzipFilter");
        registration.setOrder(5);
        return registration;
    }
}
