package com.lab.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.equipment.config.AIConfig;
import com.lab.equipment.entity.BorrowRecord;
import com.lab.equipment.entity.Equipment;
import com.lab.equipment.entity.EquipmentCategory;
import com.lab.equipment.entity.SysUser;
import com.lab.equipment.mapper.BorrowRecordMapper;
import com.lab.equipment.mapper.EquipmentCategoryMapper;
import com.lab.equipment.mapper.SysUserMapper;
import com.lab.equipment.service.EquipmentService;
import com.lab.equipment.service.LLMService;
import com.lab.equipment.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 通义千问Qwen AI服务实现（OpenAI兼容协议）
 */
@Service
@Primary  // 设置为默认实现
@Slf4j
public class QwenLLMServiceImpl implements LLMService {

    @Autowired
    private AIConfig aiConfig;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final EquipmentService equipmentService;
    private final EquipmentCategoryMapper categoryMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final SysUserMapper userMapper;
    private final RecommendationService recommendationService;

    public QwenLLMServiceImpl(EquipmentService equipmentService,
                             EquipmentCategoryMapper categoryMapper,
                             BorrowRecordMapper borrowRecordMapper,
                             SysUserMapper userMapper,
                             RecommendationService recommendationService) {
        this.equipmentService = equipmentService;
        this.categoryMapper = categoryMapper;
        this.borrowRecordMapper = borrowRecordMapper;
        this.userMapper = userMapper;
        this.recommendationService = recommendationService;
    }

    @Override
    public String chat(String userMessage, Long userId) throws Exception {
        // 检查API Key是否配置
        String apiKey = aiConfig.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("YOUR_API_KEY_HERE")) {
            return "AI服务暂未配置，请联系管理员配置讯飞星火MaaS API Key。\n\n" +
                   "配置方法：\n" +
                   "1. 访问 https://maas.xfyun.cn/packageSubscription 登录/注册\n" +
                   "2. 订阅 Astron Coding Plan 套餐\n" +
                   "3. 在 application.yml 中设置 ai.api-key 为您的API Key\n" +
                   "4. 重启应用";
        }

        // 构建系统提示词（包含设备数据库上下文）
        String systemPrompt = buildSystemPrompt(userId);

        // 构建OpenAI格式的请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiConfig.getModel());
        requestBody.put("max_tokens", aiConfig.getMaxTokens());
        requestBody.put("temperature", aiConfig.getTemperature());

        List<Map<String, String>> messages = new ArrayList<>();

        // 添加系统消息
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        // 添加用户消息
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        requestBody.put("messages", messages);

        // 发送HTTP请求
        String jsonRequest = objectMapper.writeValueAsString(requestBody);
        log.debug("Qwen AI请求: {}", jsonRequest);

        Request httpRequest = new Request.Builder()
                .url(aiConfig.getApiUrl())
                .post(RequestBody.create(jsonRequest, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("Qwen AI API调用失败: HTTP {}, response: {}", response.code(), errorBody);

                // 处理常见错误
                if (response.code() == 401) {
                    return "API认证失败，请检查API Key是否正确配置。";
                } else if (response.code() == 403) {
                    return "API访问被拒绝，请检查API Key是否有效或已过期。";
                } else if (response.code() == 429) {
                    return "请求过于频繁，请稍后重试。";
                }

                throw new RuntimeException("AI服务暂时不可用，请稍后重试。错误信息: " + errorBody);
            }

            String jsonResponse = response.body().string();
            log.debug("Qwen AI响应: {}", jsonResponse);

            // 解析OpenAI格式的响应
            Map<String, Object> chatResponse = objectMapper.readValue(jsonResponse, Map.class);

            List<Map<String, Object>> choices = (List<Map<String, Object>>) chatResponse.get("choices");
            if (choices == null || choices.isEmpty()) {
                return "AI服务返回了空响应，请稍后重试。";
            }

            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            return (String) message.get("content");
        }
    }

    @Override
    public List<Equipment> getRecommendations(Long userId) {
        // 使用综合推荐算法（协同过滤 + 时间权重 + 属性角色匹配）
        return recommendationService.hybridRecommend(userId, 5);
    }

    /**
     * 获取热门设备（兜底）
     * @deprecated 已改由 RecommendationService.getPopularEquipments 统一处理
     */
    @Deprecated
    private List<Equipment> getPopularEquipments() {
        return recommendationService.getPopularEquipments(5);
    }

    /**
     * 构建系统提示词（针对Qwen模型优化）
     */
    private String buildSystemPrompt(Long userId) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是实验室设备管理系统的智能助手，名字叫小Q。你需要用友好、专业的态度帮助用户。\n\n");
        prompt.append("你的主要职责：\n");
        prompt.append("1. 回答用户关于设备的问题（用途、规格、操作方法等）\n");
        prompt.append("2. 帮助用户了解设备的预约和借用流程\n");
        prompt.append("3. 根据用户需求推荐合适的设备\n");
        prompt.append("4. 提供设备使用指导和安全注意事项\n");
        prompt.append("5. 解答系统使用相关的问题\n\n");

        prompt.append("回答要求：\n");
        prompt.append("- 使用简洁明了的中文，语言友好亲切\n");
        prompt.append("- 如果不确定，请诚实告知用户并建议联系管理员\n");
        prompt.append("- 不要编造设备信息，只基于提供的设备列表回答\n");
        prompt.append("- 推荐设备时，可以提及设备名称、型号和主要特点\n");
        prompt.append("- 可以适当使用emoji让回答更生动（但不要过度）\n\n");

        // 添加设备分类信息
        prompt.append("【设备分类】\n");
        List<EquipmentCategory> categories = categoryMapper.selectList(null);
        for (EquipmentCategory category : categories) {
            String parentName = "";
            if (category.getParentId() != null && category.getParentId() > 0) {
                EquipmentCategory parent = categoryMapper.selectById(category.getParentId());
                if (parent != null) {
                    parentName = "（" + parent.getCategoryName() + "）";
                }
            }
            prompt.append("- ").append(category.getCategoryName()).append(parentName);
            if (category.getDescription() != null && !category.getDescription().isEmpty()) {
                prompt.append(": ").append(category.getDescription());
            }
            prompt.append("\n");
        }
        prompt.append("\n");

        // 添加设备数据库上下文
        prompt.append("【设备列表】\n");
        List<Equipment> equipments = equipmentService.list();
        int count = 0;
        for (Equipment eq : equipments) {
            if (count++ < 30) {  // 限制上下文长度，最多30个设备
                String categoryName = "";
                if (eq.getCategoryId() != null) {
                    EquipmentCategory category = categoryMapper.selectById(eq.getCategoryId());
                    if (category != null) {
                        categoryName = "[" + category.getCategoryName() + "]";
                    }
                }
                String statusText = getStatusText(eq.getStatus());
                prompt.append(String.format("- %s %s %s: %s（状态: %s）\n",
                        eq.getEquipmentName(),
                        eq.getBrand() != null ? eq.getBrand() : "",
                        eq.getModel() != null ? eq.getModel() : "",
                        eq.getDescription() != null ? eq.getDescription() : "无描述",
                        statusText));
            }
        }
        if (count > 30) {
            prompt.append("（共").append(count).append("个设备，以上为部分设备信息）\n");
        }

        // 如果用户已登录，添加个性化信息和智能推荐结果
        if (userId != null) {
            SysUser user = userMapper.selectById(userId);
            if (user != null) {
                prompt.append(String.format("\n【当前用户信息】\n用户名：%s，角色：%s，专业：%s，院系：%s\n",
                        user.getRealName() != null ? user.getRealName() : user.getUsername(),
                        getRoleText(user.getRole()),
                        user.getMajor() != null ? user.getMajor() : "未设置",
                        user.getDepartment() != null ? user.getDepartment() : "未设置"));
            }

            // 注入三种算法推荐结果说明（提升 LLM 推荐解释能力）
            try {
                Map<String, List<String>> explanations = recommendationService.getRecommendationExplanation(userId);
                if (!explanations.isEmpty()) {
                    prompt.append("\n【智能推荐结果（供参考，可向用户解释推荐原因）】\n");
                    for (Map.Entry<String, List<String>> entry : explanations.entrySet()) {
                        prompt.append("▶ ").append(entry.getKey()).append("：");
                        prompt.append(String.join("、", entry.getValue())).append("\n");
                    }
                }
            } catch (Exception e) {
                log.warn("获取推荐说明失败，不影响主流程: {}", e.getMessage());
            }
        }

        return prompt.toString();
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(String status) {
        if (status == null) return "未知";
        switch (status) {
            case "NORMAL":
                return "正常可用";
            case "BORROWED":
                return "已被借出";
            case "MAINTENANCE":
                return "维修中";
            case "SCRAPPED":
                return "已报废";
            default:
                return status;
        }
    }

    /**
     * 获取角色文本
     */
    private String getRoleText(String role) {
        if (role == null) return "未知";
        switch (role.toUpperCase()) {
            case "ADMIN":   return "管理员";
            case "TEACHER": return "教师";
            case "STUDENT": return "学生";
            default:        return role;
        }
    }
}
