package com.lab.equipment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 智谱AI配置类
 */
@Component
@ConfigurationProperties(prefix = "zhipu.ai")
public class ZhipuConfig {

    /**
     * 智谱AI API密钥
     */
    private String apiKey;

    /**
     * 智谱AI API地址
     */
    private String apiUrl;

    /**
     * 使用的模型名称
     */
    private String model;

    /**
     * 最大Token数
     */
    private Integer maxTokens;

    /**
     * 温度参数（控制随机性）
     */
    private Double temperature;

    // Getter和Setter方法

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
}
