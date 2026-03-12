package com.example.langchain4j_springboot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Web服务器配置类
 * 用于自定义服务器行为，处理协议相关问题
 */
@Configuration
public class WebServerConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebServerConfig.class);

    /**
     * 自定义Web服务器配置
     */
    @Bean
    public WebServerFactoryCustomizer<?> webServerFactoryCustomizer() {
        return factory -> {
            logger.info("Web服务器工厂已配置");
            // 基础配置，避免复杂的特定服务器配置
        };
    }
}