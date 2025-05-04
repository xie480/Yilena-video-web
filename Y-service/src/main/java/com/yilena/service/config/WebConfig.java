package com.yilena.service.config;

import com.yilena.service.dao.UserMapper;
import com.yilena.service.filter.GlobalFilter;
import com.yilena.service.interceptor.GlobalInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    //自定义的拦截器对象
    private final GlobalInterceptor globalInterceptor;
    private final RabbitTemplate rabbitTemplate;

    @Bean
    public FilterRegistrationBean<GlobalFilter> globalFilterRegistration() {
        FilterRegistrationBean<GlobalFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new GlobalFilter(rabbitTemplate));
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册自定义拦截器对象
        registry.addInterceptor(globalInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login,/search/,/chat/");//设置拦截器拦截的请求路径（ /** 表示拦截所有请求）
    }
}