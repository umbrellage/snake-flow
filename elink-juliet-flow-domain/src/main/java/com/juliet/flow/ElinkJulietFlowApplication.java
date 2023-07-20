package com.juliet.flow;

import com.alibaba.cloud.nacos.registry.NacosAutoServiceRegistration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 文件服务
 *
 * @author hejinghui
 */
@EnableFeignClients
@SpringBootApplication
@ComponentScan("com.juliet")
@MapperScan("com.juliet.flow.dao")
@RequiredArgsConstructor
@Slf4j
public class ElinkJulietFlowApplication {

    private final NacosAutoServiceRegistration nacosAutoServiceRegistration;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Success stop acm");
        }));
        SpringApplication.run(ElinkJulietFlowApplication.class, args);
        System.out.println("流程引擎启动成功\n" +
                " ▄▖ ▗ ▗  ▄▖  ▄▖  ▄▖  ▄▖  ▄▖ \n" +
                "▐ ▝ ▐ ▐ ▐▘▝ ▐▘▝ ▐▘▐ ▐ ▝ ▐ ▝ \n" +
                " ▀▚ ▐ ▐ ▐   ▐   ▐▀▀  ▀▚  ▀▚ \n" +
                "▝▄▞ ▝▄▜ ▝▙▞ ▝▙▞ ▝▙▞ ▝▄▞ ▝▄▞ \n"
        );
    }

}
