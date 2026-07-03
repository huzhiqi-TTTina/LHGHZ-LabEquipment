package com.lab.equipment.dto;

import lombok.Data;

/**
 * AI聊天请求DTO（前端请求）
 */
@Data
public class ChatRequestDTO {
    private String message;  // 用户消息
    private Long userId;    // 用户ID（可选，用于个性化推荐）
}
