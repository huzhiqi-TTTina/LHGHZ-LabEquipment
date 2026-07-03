package com.lab.equipment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 智谱AI聊天请求DTO
 */
@Data
public class ZhipuChatRequest {

    private String model;
    private List<Message> messages;
    private Double temperature;
    private Integer max_tokens;

    @Data
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    public static ZhipuChatRequest of(String model, String content) {
        ZhipuChatRequest request = new ZhipuChatRequest();
        request.setModel(model);
        Message message = new Message("user", content);
        request.setMessages(List.of(message));
        request.setTemperature(0.7);
        request.setMax_tokens(2048);
        return request;
    }
}
