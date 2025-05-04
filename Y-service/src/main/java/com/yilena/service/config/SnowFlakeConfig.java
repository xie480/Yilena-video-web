package com.yilena.service.config;
import com.yilena.service.utils.SnowFlake;
import com.yilena.service.utils.SnowFlakeProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SnowFlakeProperties.class)
public class SnowFlakeConfig {

    @Bean
    public SnowFlake snowFlake(SnowFlakeProperties properties) {
      return new SnowFlake(properties);
    }

}
