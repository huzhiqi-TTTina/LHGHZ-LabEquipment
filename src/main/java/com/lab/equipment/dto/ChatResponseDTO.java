package com.lab.equipment.dto;

import com.lab.equipment.entity.Equipment;
import lombok.Data;

import java.util.List;

/**
 * AI聊天响应DTO（前端响应）
 */
@Data
public class ChatResponseDTO {
    private String reply;  // AI回复内容
    
    private List<Equipment> recommendations;  // 推荐的设备列表（可选）
}
