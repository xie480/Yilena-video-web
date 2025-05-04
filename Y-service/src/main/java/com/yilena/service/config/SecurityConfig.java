package com.yilena.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

//所有请求无需认证
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll()  // 允许所有请求无需认证
            )
            .csrf(AbstractHttpConfigurer::disable)  // 关闭CSRF保护
            .formLogin(AbstractHttpConfigurer::disable);  // 禁用默认表单登录

        return http.build();
    }
}