package com.juliet.flow;

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
public class ElinkJulietFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElinkJulietFlowApplication.class, args);
        System.out.println("流程引擎启动成功\n" +
                " ▄▖ ▗ ▗  ▄▖  ▄▖  ▄▖  ▄▖  ▄▖ \n" +
                "▐ ▝ ▐ ▐ ▐▘▝ ▐▘▝ ▐▘▐ ▐ ▝ ▐ ▝ \n" +
                " ▀▚ ▐ ▐ ▐   ▐   ▐▀▀  ▀▚  ▀▚ \n" +
                "▝▄▞ ▝▄▜ ▝▙▞ ▝▙▞ ▝▙▞ ▝▄▞ ▝▄▞ \n"
        );
    }
}
