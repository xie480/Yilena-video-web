package com.yilena.service;

import com.yilena.service.config.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@MapperScan("com.yilena.service.dao")
@EnableConfigurationProperties(JwtProperties.class)
@SpringBootApplication
@EnableCaching
@EnableWebSocket
@EnableTransactionManagement //开启注解方式的事务管理
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
