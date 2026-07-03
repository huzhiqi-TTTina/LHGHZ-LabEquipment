package com.lab.equipment.service;

import com.lab.equipment.entity.Equipment;

import java.util.List;
import java.util.Map;

/**
 * 设备智能推荐服务接口
 * 提供三种推荐算法：协同过滤、时间权重、设备属性与角色匹配
 */
public interface RecommendationService {

    /**
     * 协同过滤推荐（User-based Collaborative Filtering）
     * 找出与目标用户借用行为相似的其他用户，推荐他们借用过但目标用户未借用的设备
     *
     * @param userId 目标用户ID（不可为null）
     * @param topN   返回的推荐数量
     * @return 推荐设备列表（按推荐分从高到低）
     */
    List<Equipment> collaborativeFilteringRecommend(Long userId, int topN);

    /**
     * 时间权重推荐（Time-weighted Frequency）
     * 对用户借用历史按时间衰减赋权，近期借用权重更高
     * 权重公式：w = exp(-lambda * days_ago)，lambda = 0.03
     *
     * @param userId 目标用户ID（不可为null）
     * @param topN   返回的推荐数量
     * @return 推荐设备列表（按时间权重评分从高到低）
     */
    List<Equipment> timeWeightedRecommend(Long userId, int topN);

    /**
     * 设备属性与角色匹配推荐（Attribute-Role Matching）
     * 根据用户角色（ADMIN/TEACHER/STUDENT）、专业、院系，
     * 匹配设备分类和描述，推荐最契合用户背景的设备
     *
     * @param userId 目标用户ID（不可为null）
     * @param topN   返回的推荐数量
     * @return 推荐设备列表（按匹配分从高到低）
     */
    List<Equipment> attributeRoleMatchRecommend(Long userId, int topN);

    /**
     * 综合融合推荐（Hybrid Recommendation）
     * 将三种算法的推荐结果加权融合，生成最终推荐列表
     * 权重：协同过滤 40%，时间权重 35%，属性角色匹配 25%
     *
     * @param userId 用户ID（为null时返回全局热门设备）
     * @param topN   返回的推荐数量
     * @return 推荐设备列表
     */
    List<Equipment> hybridRecommend(Long userId, int topN);

    /**
     * 获取全局热门设备（作为冷启动兜底）
     * 基于最近30天全局借用频率，若无记录则返回最新创建的设备
     *
     * @param topN 返回的数量
     * @return 热门设备列表
     */
    List<Equipment> getPopularEquipments(int topN);

    /**
     * 获取推荐来源说明（供 System Prompt 注入）
     * 返回各算法推荐的设备名称及原因，便于 LLM 解释
     *
     * @param userId 用户ID
     * @return Map，key 为算法名称，value 为该算法推荐的设备名列表
     */
    Map<String, List<String>> getRecommendationExplanation(Long userId);
}
