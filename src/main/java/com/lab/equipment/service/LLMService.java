package com.lab.equipment.service;

import com.lab.equipment.entity.Equipment;

import java.util.List;

/**
 * LLM服务接口（用于AI对话）
 */
public interface LLMService {
    
    /**
     * 发送消息到LLM并获取回复
     * @param userMessage 用户消息
     * @param userId 用户ID（可选，用于个性化推荐）
     * @return AI回复内容
     */
    String chat(String userMessage, Long userId) throws Exception;
    
    /**
     * 获取设备推荐（基于用户历史）
     * @param userId 用户ID
     * @return 推荐的设备列表
     */
    List<Equipment> getRecommendations(Long userId);
}
