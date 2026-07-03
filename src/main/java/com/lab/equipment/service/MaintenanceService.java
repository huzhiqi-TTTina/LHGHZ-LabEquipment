package com.lab.equipment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.equipment.common.PageResult;
import com.lab.equipment.dto.PageQueryDTO;
import com.lab.equipment.entity.Equipment;
import com.lab.equipment.entity.MaintenancePlan;
import com.lab.equipment.entity.MaintenanceRecord;
import com.lab.equipment.entity.SysUser;
import com.lab.equipment.mapper.EquipmentMapper;
import com.lab.equipment.mapper.MaintenancePlanMapper;
import com.lab.equipment.mapper.MaintenanceRecordMapper;
import com.lab.equipment.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备维护服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceService extends ServiceImpl<MaintenancePlanMapper, MaintenancePlan> {

    private final MaintenancePlanMapper planMapper;
    private final MaintenanceRecordMapper recordMapper;
    private final EquipmentMapper equipmentMapper;
    private final SysUserMapper userMapper;

    /**
     * 获取维护计划列表
     */
    public PageResult<MaintenancePlan> getPlanList(PageQueryDTO query) {
        LambdaQueryWrapper<MaintenancePlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaintenancePlan::getIsActive, 1);
        wrapper.orderByAsc(MaintenancePlan::getNextMaintenanceDate);

        Page<MaintenancePlan> page = planMapper.selectPage(
                new Page<>(query.getCurrent(), query.getSize()),
                wrapper
        );

        return PageResult.of(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }

    /**
     * 分配维护计划任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignMaintenancePlan(Long planId, Long assigneeId) {
        MaintenancePlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new RuntimeException("维护计划不存在");
        }

        plan.setResponsibleUserId(assigneeId);
        planMapper.updateById(plan);
    }

    /**
     * 获取分配给我的维护任务
     */
    public PageResult<MaintenancePlan> getMyAssignedPlans(Long userId, PageQueryDTO query) {
        LambdaQueryWrapper<MaintenancePlan> wrapper = new LambdaQueryWrapper<>();
        // 只查询分配给当前用户且有负责人的任务
        wrapper.eq(MaintenancePlan::getResponsibleUserId, userId);
        wrapper.isNotNull(MaintenancePlan::getResponsibleUserId);
        wrapper.eq(MaintenancePlan::getIsActive, 1);
        wrapper.orderByAsc(MaintenancePlan::getNextMaintenanceDate);

        Page<MaintenancePlan> page = planMapper.selectPage(
                new Page<>(query.getCurrent(), query.getSize()),
                wrapper
        );

        return PageResult.of(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }

    /**
     * 创建维护计划
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createPlan(MaintenancePlan plan) {
        planMapper.insert(plan);
        return plan.getId();
    }

    /**
     * 更新维护计划
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePlan(Long id, MaintenancePlan plan) {
        MaintenancePlan existPlan = planMapper.selectById(id);
        if (existPlan == null) {
            throw new RuntimeException("维护计划不存在");
        }
        plan.setId(id);
        planMapper.updateById(plan);
    }

    /**
     * 删除维护计划
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Long id) {
        planMapper.deleteById(id);
    }

    /**
     * 获取维护记录列表
     */
    public PageResult<MaintenanceRecord> getRecordList(PageQueryDTO query) {
        LambdaQueryWrapper<MaintenanceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(MaintenanceRecord::getMaintenanceDate);

        Page<MaintenanceRecord> page = recordMapper.selectPage(
                new Page<>(query.getCurrent(), query.getSize()),
                wrapper
        );

        // 填充计划名称和设备名称
        page.getRecords().forEach(record -> {
            if (record.getPlanId() != null) {
                MaintenancePlan plan = planMapper.selectById(record.getPlanId());
                if (plan != null) {
                    record.setPlanName(plan.getPlanName());
                }
            }
            if (record.getEquipmentId() != null) {
                Equipment equipment = equipmentMapper.selectById(record.getEquipmentId());
                if (equipment != null) {
                    record.setEquipmentName(equipment.getEquipmentName());
                }
            }
        });

        return PageResult.of(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }

    /**
     * 添加维护记录
     */
    @Transactional(rollbackFor = Exception.class)
    public Long addRecord(MaintenanceRecord record) {
        record.setRecordNo(generateRecordNo());
        record.setMaintenanceDate(LocalDateTime.now());
        recordMapper.insert(record);

        // 更新设备维护日期
        Equipment equipment = equipmentMapper.selectById(record.getEquipmentId());
        if (equipment != null && record.getNextMaintenanceDate() != null) {
            // 更新设备信息（可选）
        }

        return record.getId();
    }

    /**
     * 获取需要提醒的维护计划
     */
    public List<MaintenancePlan> getPendingReminders() {
        LocalDate today = LocalDate.now();
        return planMapper.findPendingReminders(today);
    }

    /**
     * 完成维护并更新计划
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeMaintenance(Long planId, Long equipmentId, String content,
                                  String result, Long maintainerId) {
        log.info("开始完成维护任务: planId={}, equipmentId={}, maintainerId={}", planId, equipmentId, maintainerId);

        MaintenancePlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new RuntimeException("维护计划不存在");
        }

        log.info("当前维护计划状态: responsibleUserId={}, lastCompletedDate={}, nextMaintenanceDate={}",
            plan.getResponsibleUserId(), plan.getLastCompletedDate(), plan.getNextMaintenanceDate());

        // 检查是否已经完成了本次维护（如果今天已经完成过）
        if (plan.getLastCompletedDate() != null) {
            LocalDate lastCompleted = plan.getLastCompletedDate();
            LocalDate nextDue = lastCompleted.plusDays(plan.getMaintenanceCycle());
            if (LocalDate.now().isBefore(nextDue)) {
                throw new RuntimeException("本次维护周期尚未结束，请在 " + nextDue + " 后再执行");
            }
        }

        LocalDate today = LocalDate.now();
        LocalDate nextDate = today.plusDays(plan.getMaintenanceCycle());

        // 保存当前负责人ID，确保更新后不会丢失
        Long currentResponsibleUserId = plan.getResponsibleUserId();
        log.info("保存当前负责人ID: {}", currentResponsibleUserId);

        // 添加维护记录
        MaintenanceRecord record = new MaintenanceRecord();
        record.setPlanId(planId);
        record.setEquipmentId(equipmentId);
        record.setMaintenanceType(plan.getMaintenanceType());
        record.setMaintenanceContent(content);
        record.setMaintenanceResult(result);
        record.setMaintainerId(maintainerId);
        record.setNextMaintenanceDate(nextDate);
        addRecord(record);

        log.info("维护记录已添加，准备更新维护计划");

        // 更新维护计划：保留负责人，更新最后完成日期和下次维护日期
        LambdaUpdateWrapper<MaintenancePlan> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MaintenancePlan::getId, planId)
                    .set(MaintenancePlan::getLastCompletedDate, today)
                    .set(MaintenancePlan::getNextMaintenanceDate, nextDate);

        int updated = planMapper.update(null, updateWrapper);
        if (updated <= 0) {
            throw new RuntimeException("更新维护计划失败");
        }

        log.info("维护计划更新完成: updated={}, responsibleUserId应保持为={}", updated, currentResponsibleUserId);

        // 验证更新后的数据
        MaintenancePlan updatedPlan = planMapper.selectById(planId);
        log.info("更新后的维护计划: responsibleUserId={}, lastCompletedDate={}, nextMaintenanceDate={}",
            updatedPlan.getResponsibleUserId(), updatedPlan.getLastCompletedDate(), updatedPlan.getNextMaintenanceDate());

        // 确保负责人ID没有被意外清除
        if (currentResponsibleUserId != null && !currentResponsibleUserId.equals(updatedPlan.getResponsibleUserId())) {
            log.error("警告:负责人ID被意外清除! 期望:{}, 实际:{}", currentResponsibleUserId, updatedPlan.getResponsibleUserId());
            // 尝试恢复
            updatedPlan.setResponsibleUserId(currentResponsibleUserId);
            planMapper.updateById(updatedPlan);
            log.info("已恢复负责人ID");
        }
    }

    /**
     * 计算下次维护日期（从指定日期开始）
     */
    private LocalDate calculateNextMaintenanceDate(MaintenancePlan plan, LocalDate fromDate) {
        return fromDate.plusDays(plan.getMaintenanceCycle());
    }

    /**
     * 计算下次维护日期（从今天开始）
     */
    private LocalDate calculateNextMaintenanceDate(MaintenancePlan plan) {
        return LocalDate.now().plusDays(plan.getMaintenanceCycle());
    }

    /**
     * 生成记录编号
     */
    private String generateRecordNo() {
        return "MREC" + System.currentTimeMillis();
    }

    /**
     * 获取所有老师用户（用于任务分配）
     */
    public List<SysUser> getAllTeachers() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getRole, "TEACHER");
        wrapper.eq(SysUser::getStatus, 1);  // 只返回启用状态的用户
        wrapper.orderByAsc(SysUser::getRealName);
        return userMapper.selectList(wrapper);
    }
}
