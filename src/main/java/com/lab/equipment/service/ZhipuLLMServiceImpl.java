package com.lab.equipment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.equipment.config.ZhipuConfig;
import com.lab.equipment.dto.ZhipuChatRequest;
import com.lab.equipment.dto.ZhipuChatResponse;
import com.lab.equipment.entity.BorrowRecord;
import com.lab.equipment.entity.Equipment;
import com.lab.equipment.entity.EquipmentCategory;
import com.lab.equipment.entity.SysUser;
import com.lab.equipment.mapper.BorrowRecordMapper;
import com.lab.equipment.mapper.EquipmentCategoryMapper;
import com.lab.equipment.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 智谱AI服务实现
 */
@Service
@Slf4j
public class ZhipuLLMServiceImpl implements LLMService {

    @Autowired
    private ZhipuConfig zhipuConfig;

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

    public ZhipuLLMServiceImpl(EquipmentService equipmentService,
                              EquipmentCategoryMapper categoryMapper,
                              BorrowRecordMapper borrowRecordMapper,
                              SysUserMapper userMapper) {
        this.equipmentService = equipmentService;
        this.categoryMapper = categoryMapper;
        this.borrowRecordMapper = borrowRecordMapper;
        this.userMapper = userMapper;
    }

    @Override
    public String chat(String userMessage, Long userId) throws Exception {
        // 检查API Key是否配置
        String apiKey = zhipuConfig.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "AI服务暂未配置，请联系管理员配置智谱AI API Key。\n\n配置方法：在 application.yml 中设置 zhipu.ai.api-key 为您的智谱AI API密钥。\n\n您可以访问 https://open.bigmodel.cn/ 注册并获取API Key。";
        }

        // 构建系统提示词（包含设备数据库上下文）
        String systemPrompt = buildSystemPrompt(userId);

        // 构建请求体
        ZhipuChatRequest request = new ZhipuChatRequest();
        request.setModel(zhipuConfig.getModel());
        request.setMessages(Arrays.asList(
                new ZhipuChatRequest.Message("system", systemPrompt),
                new ZhipuChatRequest.Message("user", userMessage)
        ));

        // 发送HTTP请求
        String jsonRequest = objectMapper.writeValueAsString(request);
        Request httpRequest = new Request.Builder()
                .url(zhipuConfig.getApiUrl())
                .post(RequestBody.create(jsonRequest, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("AI API调用失败: {}, response: {}", response, errorBody);
                throw new RuntimeException("AI服务暂时不可用，请稍后重试。错误信息: " + errorBody);
            }

            String jsonResponse = response.body().string();
            log.debug("AI响应: {}", jsonResponse);

            ZhipuChatResponse chatResponse = objectMapper.readValue(jsonResponse, ZhipuChatResponse.class);

            if (chatResponse.getChoices() == null || chatResponse.getChoices().isEmpty()) {
                return "AI服务返回了空响应，请稍后重试。";
            }

            return chatResponse.getChoices().get(0).getMessage().getContent();
        }
    }

    @Override
    public List<Equipment> getRecommendations(Long userId) {
        List<Equipment> recommendations = new ArrayList<>();

        if (userId == null) {
            // 未登录用户，返回热门设备
            return getPopularEquipments();
        }

        // 1. 获取用户最近30天的借用记录
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Date thirtyDaysAgoDate = Date.from(thirtyDaysAgo.atZone(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getUserId, userId)
                .ge(BorrowRecord::getBorrowDate, thirtyDaysAgoDate)
                .orderByDesc(BorrowRecord::getBorrowDate);
        List<BorrowRecord> borrowRecords = borrowRecordMapper.selectList(wrapper);

        // 统计设备使用频率
        Map<Long, Integer> equipmentCount = new HashMap<>();
        for (BorrowRecord record : borrowRecords) {
            if (record.getEquipmentId() != null) {
                equipmentCount.merge(record.getEquipmentId(), 1, Integer::sum);
            }
        }

        // 按使用频率排序
        List<Long> sortedEquipmentIds = equipmentCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        // 查询设备详情
        for (Long equipmentId : sortedEquipmentIds) {
            Equipment equipment = equipmentService.getById(equipmentId);
            if (equipment != null) {
                recommendations.add(equipment);
            }
        }

        // 如果推荐数量不足5个，补充热门设备
        if (recommendations.size() < 5) {
            List<Equipment> popularEquipments = getPopularEquipments();
            for (Equipment eq : popularEquipments) {
                if (recommendations.size() >= 5) break;
                if (recommendations.stream().noneMatch(e -> e.getId().equals(eq.getId()))) {
                    recommendations.add(eq);
                }
            }
        }

        return recommendations;
    }

    /**
     * 获取热门设备（使用频率最高的设备）
     */
    private List<Equipment> getPopularEquipments() {
        // 获取最近30天借用频率最高的设备
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Date thirtyDaysAgoDate = Date.from(thirtyDaysAgo.atZone(ZoneId.systemDefault()).toInstant());

        List<BorrowRecord> borrowRecords = borrowRecordMapper.selectList(
                new LambdaQueryWrapper<BorrowRecord>()
                        .ge(BorrowRecord::getBorrowDate, thirtyDaysAgoDate)
        );

        // 统计设备使用频率
        Map<Long, Integer> equipmentCount = new HashMap<>();
        for (BorrowRecord record : borrowRecords) {
            if (record.getEquipmentId() != null) {
                equipmentCount.merge(record.getEquipmentId(), 1, Integer::sum);
            }
        }

        // 按使用频率排序
        List<Long> sortedEquipmentIds = equipmentCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        List<Equipment> popularEquipments = new ArrayList<>();
        for (Long equipmentId : sortedEquipmentIds) {
            Equipment equipment = equipmentService.getById(equipmentId);
            if (equipment != null) {
                popularEquipments.add(equipment);
            }
        }

        // 如果没有借用记录，返回最新添加的设备
        if (popularEquipments.isEmpty()) {
            LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(Equipment::getCreateTime).last("LIMIT 5");
            popularEquipments = equipmentService.list(wrapper);
        }

        return popularEquipments;
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(Long userId) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个实验室设备管理系统的人工智能助手。你的职责是：\n");
        prompt.append("1. 回答用户关于设备的问题（用途、规格、操作方法等）\n");
        prompt.append("2. 帮助用户了解设备的预约和借用流程\n");
        prompt.append("3. 根据用户需求推荐合适的设备\n");
        prompt.append("4. 提供设备使用指导和安全注意事项\n\n");

        prompt.append("回答要求：\n");
        prompt.append("- 使用简洁明了的中文\n");
        prompt.append("- 如果不确定，请告知用户并建议联系管理员\n");
        prompt.append("- 不要编造设备信息，只基于提供的设备列表回答\n");
        prompt.append("- 推荐设备时，可以提及设备名称和主要特点\n\n");

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

        // 如果用户已登录，添加个性化信息
        if (userId != null) {
            SysUser user = userMapper.selectById(userId);
            if (user != null) {
                prompt.append(String.format("\n【当前用户信息】\n用户名：%s，专业：%s，院系：%s\n",
                        user.getRealName() != null ? user.getRealName() : user.getUsername(),
                        user.getMajor() != null ? user.getMajor() : "未设置",
                        user.getDepartment() != null ? user.getDepartment() : "未设置"));
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
}
