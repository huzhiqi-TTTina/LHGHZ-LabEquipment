package com.lab.equipment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI服务配置类（支持讯飞星辰等多种AI服务提供商）
 */
@Component
@ConfigurationProperties(prefix = "ai")
@Data
public class AIConfig {

    /**
     * AI服务提供商
     */
    private String provider = "xunfei-astron";

    /**
     * API地址
     */
    private String apiUrl;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 最大Token数
     */
    private Integer maxTokens = 4096;

    /**
     * 温度参数（控制随机性，0-1）
     */
    private Double temperature = 0.7;

    /**
     * 超时时间（秒）
     */
    private Integer timeout = 120;
}
