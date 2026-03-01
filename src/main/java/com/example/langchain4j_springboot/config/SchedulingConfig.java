package com.example.langchain4j_springboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置类
 * 启用Spring的定时任务功能
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // 此类用于启用定时任务功能
    // 实际的定时任务逻辑将在相应的服务类中实现
}