package com.lab.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.equipment.entity.BorrowRecord;
import com.lab.equipment.entity.Equipment;
import com.lab.equipment.entity.EquipmentCategory;
import com.lab.equipment.entity.SysUser;
import com.lab.equipment.mapper.BorrowRecordMapper;
import com.lab.equipment.mapper.EquipmentCategoryMapper;
import com.lab.equipment.mapper.SysUserMapper;
import com.lab.equipment.service.EquipmentService;
import com.lab.equipment.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备智能推荐服务实现
 *
 * <p>包含三种推荐算法：</p>
 * <ol>
 *   <li><b>协同过滤</b>：基于用户行为相似度（Jaccard 相似度）</li>
 *   <li><b>时间权重</b>：指数衰减加权的借用频率统计</li>
 *   <li><b>属性角色匹配</b>：用户画像 × 设备分类 关键词匹配</li>
 * </ol>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final BorrowRecordMapper borrowRecordMapper;
    private final EquipmentCategoryMapper categoryMapper;
    private final SysUserMapper userMapper;
    private final EquipmentService equipmentService;

    // ==================== 算法参数常量 ====================

    /**
     * 时间权重衰减系数 λ（对应半衰期约 23 天）
     * 时间权重公式：w(t) = e^(-λ * days_ago)
     */
    private static final double TIME_DECAY_LAMBDA = 0.03;

    /**
     * 时间权重算法的历史窗口（天）
     */
    private static final int TIME_WINDOW_DAYS = 90;

    /**
     * 协同过滤：相似用户数量 K
     */
    private static final int CF_TOP_K_SIMILAR_USERS = 10;

    /**
     * 协同过滤：最低 Jaccard 相似度阈值
     */
    private static final double CF_MIN_SIMILARITY = 0.05;

    /**
     * 混合推荐权重配置（三种算法之和为 1.0）
     */
    private static final double WEIGHT_COLLABORATIVE = 0.40;
    private static final double WEIGHT_TIME_WEIGHTED = 0.35;
    private static final double WEIGHT_ATTR_ROLE     = 0.25;

    // ==================== 关键词 → 设备分类 权重映射 ====================

    /**
     * 专业/院系关键词映射到设备分类名称权重
     * key: 关键词（用于 contains 匹配 major/department）
     * value: Map<分类名称, 额外权重>
     */
    private static final Map<String, Map<String, Double>> KEYWORD_CATEGORY_WEIGHTS;

    static {
        KEYWORD_CATEGORY_WEIGHTS = new LinkedHashMap<>();
        // 计算机/信息相关
        KEYWORD_CATEGORY_WEIGHTS.put("计算机", mapOf("计算机设备", 3.0, "电子仪器", 0.5));
        KEYWORD_CATEGORY_WEIGHTS.put("信息", mapOf("计算机设备", 2.5, "电子仪器", 0.5));
        KEYWORD_CATEGORY_WEIGHTS.put("软件", mapOf("计算机设备", 2.5));
        KEYWORD_CATEGORY_WEIGHTS.put("网络", mapOf("计算机设备", 2.0, "电子仪器", 1.0));
        // 电子/电气相关
        KEYWORD_CATEGORY_WEIGHTS.put("电子", mapOf("电子仪器", 3.0, "计算机设备", 0.5));
        KEYWORD_CATEGORY_WEIGHTS.put("电气", mapOf("电子仪器", 2.5, "机械设备", 0.5));
        KEYWORD_CATEGORY_WEIGHTS.put("通信", mapOf("电子仪器", 2.0, "计算机设备", 1.0));
        // 机械/工程相关
        KEYWORD_CATEGORY_WEIGHTS.put("机械", mapOf("机械设备", 3.0));
        KEYWORD_CATEGORY_WEIGHTS.put("工程", mapOf("机械设备", 2.0, "电子仪器", 0.5));
        KEYWORD_CATEGORY_WEIGHTS.put("制造", mapOf("机械设备", 2.5));
        // 化学/化工相关
        KEYWORD_CATEGORY_WEIGHTS.put("化学", mapOf("化学设备", 3.0));
        KEYWORD_CATEGORY_WEIGHTS.put("化工", mapOf("化学设备", 3.0));
        KEYWORD_CATEGORY_WEIGHTS.put("材料", mapOf("化学设备", 2.0, "物理设备", 1.0));
        // 物理/光学相关
        KEYWORD_CATEGORY_WEIGHTS.put("物理", mapOf("物理设备", 3.0, "电子仪器", 0.5));
        KEYWORD_CATEGORY_WEIGHTS.put("光学", mapOf("物理设备", 2.0));
        // 生物/医学相关
        KEYWORD_CATEGORY_WEIGHTS.put("生物", mapOf("生物设备", 3.0, "化学设备", 0.5));
        KEYWORD_CATEGORY_WEIGHTS.put("医学", mapOf("生物设备", 2.5, "化学设备", 0.5));
        KEYWORD_CATEGORY_WEIGHTS.put("医疗", mapOf("生物设备", 2.5));
    }

    /**
     * 角色 → 设备分类默认权重
     * key: 角色字符串（STUDENT/TEACHER/ADMIN）
     * value: Map<分类名称, 基础权重>
     */
    private static final Map<String, Map<String, Double>> ROLE_CATEGORY_WEIGHTS;

    static {
        ROLE_CATEGORY_WEIGHTS = new HashMap<>();
        ROLE_CATEGORY_WEIGHTS.put("STUDENT", mapOf(
                "计算机设备", 1.5,
                "电子仪器", 1.0,
                "物理设备", 0.8,
                "化学设备", 0.8,
                "机械设备", 0.6,
                "生物设备", 0.6
        ));
        ROLE_CATEGORY_WEIGHTS.put("TEACHER", mapOf(
                "电子仪器", 2.0,
                "物理设备", 1.8,
                "化学设备", 1.8,
                "机械设备", 1.5,
                "生物设备", 1.5,
                "计算机设备", 1.0
        ));
        ROLE_CATEGORY_WEIGHTS.put("ADMIN", mapOf(
                "计算机设备", 1.2,
                "电子仪器", 1.0,
                "机械设备", 1.0,
                "化学设备", 1.0,
                "物理设备", 1.0,
                "生物设备", 1.0
        ));
    }

    // ==================== 一、协同过滤推荐 ====================

    @Override
    public List<Equipment> collaborativeFilteringRecommend(Long userId, int topN) {
        log.debug("[协同过滤] 开始为用户 {} 生成推荐", userId);

        // 1. 获取目标用户的借用设备集合
        List<BorrowRecord> userRecords = borrowRecordMapper.selectList(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getUserId, userId)
                        .isNotNull(BorrowRecord::getEquipmentId)
        );
        if (userRecords.isEmpty()) {
            log.debug("[协同过滤] 用户 {} 无借用记录，协同过滤跳过", userId);
            return Collections.emptyList();
        }

        Set<Long> userEquipmentIds = userRecords.stream()
                .map(BorrowRecord::getEquipmentId)
                .collect(Collectors.toSet());

        // 2. 获取所有其他用户的借用记录
        List<BorrowRecord> allOtherRecords = borrowRecordMapper.selectList(
                new LambdaQueryWrapper<BorrowRecord>()
                        .ne(BorrowRecord::getUserId, userId)
                        .isNotNull(BorrowRecord::getEquipmentId)
        );
        if (allOtherRecords.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 构建其他用户的借用设备集合 Map<userId, Set<equipmentId>>
        Map<Long, Set<Long>> otherUserEquipments = new HashMap<>();
        // 同时记录各用户对各设备的借用次数 Map<userId, Map<equipmentId, count>>
        Map<Long, Map<Long, Integer>> otherUserBorrowCount = new HashMap<>();

        for (BorrowRecord record : allOtherRecords) {
            Long uid = record.getUserId();
            Long eid = record.getEquipmentId();
            otherUserEquipments.computeIfAbsent(uid, k -> new HashSet<>()).add(eid);
            otherUserBorrowCount.computeIfAbsent(uid, k -> new HashMap<>())
                    .merge(eid, 1, Integer::sum);
        }

        // 4. 计算 Jaccard 相似度，取 Top-K 相似用户
        List<Map.Entry<Long, Double>> similarities = new ArrayList<>();
        for (Map.Entry<Long, Set<Long>> entry : otherUserEquipments.entrySet()) {
            Long otherUserId = entry.getKey();
            Set<Long> otherEquipments = entry.getValue();

            double similarity = jaccardSimilarity(userEquipmentIds, otherEquipments);
            if (similarity >= CF_MIN_SIMILARITY) {
                similarities.add(Map.entry(otherUserId, similarity));
            }
        }
        similarities.sort(Map.Entry.<Long, Double>comparingByValue().reversed());
        List<Map.Entry<Long, Double>> topKUsers = similarities.stream()
                .limit(CF_TOP_K_SIMILAR_USERS)
                .collect(Collectors.toList());

        if (topKUsers.isEmpty()) {
            log.debug("[协同过滤] 用户 {} 没有找到相似用户", userId);
            return Collections.emptyList();
        }

        // 5. 计算候选设备评分：推荐分 = Σ(similarity * count)
        Map<Long, Double> candidateScores = new HashMap<>();
        for (Map.Entry<Long, Double> similarUser : topKUsers) {
            Long simUserId = similarUser.getKey();
            double sim = similarUser.getValue();
            Map<Long, Integer> borrowCounts = otherUserBorrowCount.getOrDefault(simUserId, Collections.emptyMap());

            for (Map.Entry<Long, Integer> eCount : borrowCounts.entrySet()) {
                Long eid = eCount.getKey();
                int count = eCount.getValue();
                // 跳过目标用户已借用过的设备
                if (!userEquipmentIds.contains(eid)) {
                    candidateScores.merge(eid, sim * count, Double::sum);
                }
            }
        }

        log.debug("[协同过滤] 用户 {} 找到 {} 个候选设备", userId, candidateScores.size());
        return getEquipmentsByScores(candidateScores, topN);
    }

    // ==================== 二、时间权重推荐 ====================

    @Override
    public List<Equipment> timeWeightedRecommend(Long userId, int topN) {
        log.debug("[时间权重] 开始为用户 {} 生成推荐", userId);

        LocalDateTime windowStart = LocalDateTime.now().minusDays(TIME_WINDOW_DAYS);
        List<BorrowRecord> records = borrowRecordMapper.selectList(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getUserId, userId)
                        .ge(BorrowRecord::getBorrowDate, windowStart)
                        .isNotNull(BorrowRecord::getEquipmentId)
                        .orderByDesc(BorrowRecord::getBorrowDate)
        );

        if (records.isEmpty()) {
            log.debug("[时间权重] 用户 {} 在最近 {} 天无借用记录", userId, TIME_WINDOW_DAYS);
            return Collections.emptyList();
        }

        // 计算每个设备的时间权重评分
        Map<Long, Double> scores = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (BorrowRecord record : records) {
            if (record.getBorrowDate() == null) continue;
            long daysDiff = ChronoUnit.DAYS.between(record.getBorrowDate(), now);
            // 指数衰减权重：w(t) = e^(-λ * days)
            double weight = Math.exp(-TIME_DECAY_LAMBDA * daysDiff);
            scores.merge(record.getEquipmentId(), weight, Double::sum);
        }

        log.debug("[时间权重] 用户 {} 共 {} 个设备有历史记录", userId, scores.size());
        return getEquipmentsByScores(scores, topN);
    }

    // ==================== 三、设备属性与角色匹配推荐 ====================

    @Override
    public List<Equipment> attributeRoleMatchRecommend(Long userId, int topN) {
        log.debug("[属性角色匹配] 开始为用户 {} 生成推荐", userId);

        // 1. 获取用户信息
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            log.debug("[属性角色匹配] 未找到用户 {}，跳过", userId);
            return Collections.emptyList();
        }

        String role       = user.getRole() != null ? user.getRole().toUpperCase() : "STUDENT";
        String major      = user.getMajor() != null ? user.getMajor() : "";
        String department = user.getDepartment() != null ? user.getDepartment() : "";
        String userProfile = major + " " + department;  // 合并用于关键词匹配

        // 2. 构建分类权重：角色默认权重 + 关键词加权
        Map<String, Double> categoryWeights = new HashMap<>();

        // 叠加角色默认权重
        Map<String, Double> roleWeights = ROLE_CATEGORY_WEIGHTS.getOrDefault(role, Collections.emptyMap());
        roleWeights.forEach((cat, w) -> categoryWeights.merge(cat, w, Double::sum));

        // 叠加关键词匹配权重
        for (Map.Entry<String, Map<String, Double>> entry : KEYWORD_CATEGORY_WEIGHTS.entrySet()) {
            if (userProfile.contains(entry.getKey())) {
                entry.getValue().forEach((cat, w) -> categoryWeights.merge(cat, w, Double::sum));
                log.debug("[属性角色匹配] 用户 {} 匹配关键词「{}」", userId, entry.getKey());
            }
        }

        if (categoryWeights.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 加载分类信息，建立分类名 → ID 映射
        List<EquipmentCategory> categories = categoryMapper.selectList(null);
        Map<String, Long> categoryNameToId = categories.stream()
                .collect(Collectors.toMap(EquipmentCategory::getCategoryName, EquipmentCategory::getId,
                        (a, b) -> a));  // 重名时取第一个

        // 4. 遍历所有 NORMAL 状态设备，计算匹配评分
        List<Equipment> allEquipments = equipmentService.list(
                new LambdaQueryWrapper<Equipment>()
                        .ne(Equipment::getStatus, "SCRAPPED")  // 排除报废设备
        );

        Map<Long, Double> scores = new HashMap<>();
        for (Equipment equipment : allEquipments) {
            double score = 0.0;

            if (equipment.getCategoryId() != null) {
                // 查找该设备的分类名称
                EquipmentCategory cat = categories.stream()
                        .filter(c -> c.getId().equals(equipment.getCategoryId()))
                        .findFirst().orElse(null);

                if (cat != null) {
                    // 直接分类权重
                    score += categoryWeights.getOrDefault(cat.getCategoryName(), 0.0);

                    // 若是子分类，叠加父分类权重（父分类权重 × 0.6）
                    if (cat.getParentId() != null && cat.getParentId() > 0) {
                        EquipmentCategory parent = categoryMapper.selectById(cat.getParentId());
                        if (parent != null) {
                            score += categoryWeights.getOrDefault(parent.getCategoryName(), 0.0) * 0.6;
                        }
                    }
                }
            }

            // 额外：NORMAL 状态加分，避免推荐不可用设备
            if ("NORMAL".equals(equipment.getStatus())) {
                score += 0.3;
            }

            if (score > 0) {
                scores.put(equipment.getId(), score);
            }
        }

        log.debug("[属性角色匹配] 用户 {} (角色:{}, 专业:{}) 得到 {} 个候选设备",
                userId, role, major, scores.size());
        return getEquipmentsByScores(scores, topN);
    }

    // ==================== 四、综合融合推荐 ====================

    @Override
    public List<Equipment> hybridRecommend(Long userId, int topN) {
        if (userId == null) {
            return getPopularEquipments(topN);
        }

        log.info("[融合推荐] 开始为用户 {} 生成混合推荐（topN={}）", userId, topN);

        // 分别执行三种算法，内部请求更多候选量以供融合
        int candidateN = topN * 3;
        List<Equipment> cfList     = collaborativeFilteringRecommend(userId, candidateN);
        List<Equipment> twList     = timeWeightedRecommend(userId, candidateN);
        List<Equipment> arList     = attributeRoleMatchRecommend(userId, candidateN);

        log.debug("[融合推荐] 协同过滤 {} 个，时间权重 {} 个，属性角色 {} 个",
                cfList.size(), twList.size(), arList.size());

        // 归一化各算法评分并加权融合
        Map<Long, Double> hybridScores = new HashMap<>();

        addNormalizedScores(hybridScores, cfList, WEIGHT_COLLABORATIVE);
        addNormalizedScores(hybridScores, twList, WEIGHT_TIME_WEIGHTED);
        addNormalizedScores(hybridScores, arList, WEIGHT_ATTR_ROLE);

        // 若所有算法均无结果，降级到热门
        if (hybridScores.isEmpty()) {
            log.info("[融合推荐] 用户 {} 三种算法均无结果，降级到全局热门", userId);
            return getPopularEquipments(topN);
        }

        // 按融合分数降序排序，取 TopN
        List<Long> sortedIds = hybridScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Equipment> result = new ArrayList<>();
        for (Long eid : sortedIds) {
            Equipment equipment = equipmentService.getById(eid);
            if (equipment != null) {
                result.add(equipment);
            }
        }

        // 不足 topN 则补充热门设备
        if (result.size() < topN) {
            List<Equipment> popular = getPopularEquipments(topN);
            Set<Long> existingIds = result.stream().map(Equipment::getId).collect(Collectors.toSet());
            for (Equipment eq : popular) {
                if (result.size() >= topN) break;
                if (!existingIds.contains(eq.getId())) {
                    result.add(eq);
                }
            }
        }

        log.info("[融合推荐] 用户 {} 最终推荐 {} 个设备", userId, result.size());
        return result;
    }

    // ==================== 五、全局热门设备 ====================

    @Override
    public List<Equipment> getPopularEquipments(int topN) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<BorrowRecord> records = borrowRecordMapper.selectList(
                new LambdaQueryWrapper<BorrowRecord>()
                        .ge(BorrowRecord::getBorrowDate, thirtyDaysAgo)
                        .isNotNull(BorrowRecord::getEquipmentId)
        );

        Map<Long, Long> equipmentCount = records.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getEquipmentId, Collectors.counting()));

        List<Long> topIds = equipmentCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Equipment> popular = new ArrayList<>();
        for (Long eid : topIds) {
            Equipment equipment = equipmentService.getById(eid);
            if (equipment != null) popular.add(equipment);
        }

        // 无借用记录则返回最新设备
        if (popular.isEmpty()) {
            popular = equipmentService.list(
                    new LambdaQueryWrapper<Equipment>()
                            .ne(Equipment::getStatus, "SCRAPPED")
                            .orderByDesc(Equipment::getCreateTime)
                            .last("LIMIT " + topN)
            );
        }
        return popular;
    }

    // ==================== 六、推荐来源说明 ====================

    @Override
    public Map<String, List<String>> getRecommendationExplanation(Long userId) {
        Map<String, List<String>> explanation = new LinkedHashMap<>();

        if (userId == null) {
            List<Equipment> popular = getPopularEquipments(5);
            explanation.put("全局热门",
                    popular.stream().map(Equipment::getEquipmentName).collect(Collectors.toList()));
            return explanation;
        }

        List<Equipment> cfList = collaborativeFilteringRecommend(userId, 5);
        List<Equipment> twList = timeWeightedRecommend(userId, 5);
        List<Equipment> arList = attributeRoleMatchRecommend(userId, 5);

        if (!cfList.isEmpty()) {
            explanation.put("协同过滤（与你行为相似的用户也借用了）",
                    cfList.stream().map(Equipment::getEquipmentName).collect(Collectors.toList()));
        }
        if (!twList.isEmpty()) {
            explanation.put("时间权重（基于你近期借用偏好）",
                    twList.stream().map(Equipment::getEquipmentName).collect(Collectors.toList()));
        }
        if (!arList.isEmpty()) {
            explanation.put("属性匹配（根据你的专业和角色推荐）",
                    arList.stream().map(Equipment::getEquipmentName).collect(Collectors.toList()));
        }

        return explanation;
    }

    // ==================== 私有工具方法 ====================

    /**
     * 计算两个集合的 Jaccard 相似度
     * sim(A, B) = |A ∩ B| / |A ∪ B|
     */
    private double jaccardSimilarity(Set<Long> setA, Set<Long> setB) {
        if (setA.isEmpty() || setB.isEmpty()) return 0.0;
        Set<Long> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        Set<Long> union = new HashSet<>(setA);
        union.addAll(setB);
        return (double) intersection.size() / union.size();
    }

    /**
     * 将推荐列表中的设备按位置赋予归一化分数（位置越靠前分数越高），
     * 然后乘以 weight 后叠加到 hybridScores 中
     */
    private void addNormalizedScores(Map<Long, Double> hybridScores,
                                     List<Equipment> recommendations,
                                     double weight) {
        int n = recommendations.size();
        if (n == 0) return;
        for (int i = 0; i < n; i++) {
            Long eid = recommendations.get(i).getId();
            // 归一化分数：位置 0 → 1.0，位置 n-1 → 1/n
            double normalizedScore = (double) (n - i) / n;
            hybridScores.merge(eid, normalizedScore * weight, Double::sum);
        }
    }

    /**
     * 根据评分 Map 查询并返回 TopN 设备
     */
    private List<Equipment> getEquipmentsByScores(Map<Long, Double> scores, int topN) {
        if (scores.isEmpty()) return Collections.emptyList();

        List<Long> sortedIds = scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Equipment> result = new ArrayList<>();
        for (Long eid : sortedIds) {
            Equipment equipment = equipmentService.getById(eid);
            if (equipment != null) result.add(equipment);
        }
        return result;
    }

    /**
     * 创建 Map<String, Double> 的辅助方法（Java 9+ Map.of 最多支持 10 条，此处不受限）
     */
    @SafeVarargs
    private static Map<String, Double> mapOf(Object... pairs) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length - 1; i += 2) {
            map.put((String) pairs[i], (Double) pairs[i + 1]);
        }
        return map;
    }
}
