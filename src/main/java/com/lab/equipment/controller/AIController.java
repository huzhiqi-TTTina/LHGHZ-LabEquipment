package com.lab.equipment.controller;

import com.lab.equipment.common.Result;
import com.lab.equipment.dto.ChatRequestDTO;
import com.lab.equipment.dto.ChatResponseDTO;
import com.lab.equipment.entity.Equipment;
import com.lab.equipment.service.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI智能助手控制器
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AIController {

    private final LLMService llmService;

    /**
     * AI问答
     */
    @PostMapping("/chat")
    public Result<ChatResponseDTO> chat(@RequestBody ChatRequestDTO request) {
        try {
            String reply = llmService.chat(request.getMessage(), request.getUserId());

            ChatResponseDTO response = new ChatResponseDTO();
            response.setReply(reply);

            // 如果消息包含"推荐"关键词，附加设备推荐
            if (request.getMessage().contains("推荐") || request.getMessage().contains("建议")) {
                List<Equipment> recommendations = llmService.getRecommendations(request.getUserId());
                // 转换为DTO
                if (!recommendations.isEmpty()) {
                    response.setRecommendations(recommendations);
                }
            }

            return Result.success(response);
        } catch (Exception e) {
            log.error("AI聊天失败", e);
            return Result.error("AI服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 获取设备推荐
     */
    @GetMapping("/recommend")
    public Result<List<Equipment>> getRecommendations(@RequestParam(required = false) Long userId) {
        try {
            List<Equipment> recommendations = llmService.getRecommendations(userId);
            return Result.success(recommendations);
        } catch (Exception e) {
            log.error("获取推荐失败", e);
            return Result.error("获取推荐失败");
        }
    }

    /**
     * 检查AI服务状态
     */
    @GetMapping("/status")
    public Result<String> checkStatus() {
        try {
            String reply = llmService.chat("你好", null);
            return Result.success("AI服务正常");
        } catch (Exception e) {
            log.error("AI服务状态检查失败", e);
            return Result.error("AI服务暂不可用");
        }
    }
}
