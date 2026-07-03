package com.lab.equipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.equipment.common.PageResult;
import com.lab.equipment.common.Result;
import com.lab.equipment.dto.PageQueryDTO;
import com.lab.equipment.entity.MaintenancePlan;
import com.lab.equipment.entity.MaintenanceRecord;
import com.lab.equipment.entity.SysUser;
import com.lab.equipment.mapper.SysUserMapper;
import com.lab.equipment.security.CustomeUserDetails;
import com.lab.equipment.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备维护控制器
 */
@RestController
@RequestMapping("/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    /**
     * 获取维护计划列表
     */
    @GetMapping("/plans")
    public Result<PageResult<MaintenancePlan>> getPlans(PageQueryDTO query) {
        PageResult<MaintenancePlan> result = maintenanceService.getPlanList(query);
        return Result.success(result);
    }

    /**
     * 创建维护计划（管理员/老师）
     */
    @PostMapping("/plans")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Long> createPlan(@RequestBody MaintenancePlan plan) {
        Long id = maintenanceService.createPlan(plan);
        return Result.success("维护计划创建成功", id);
    }

    /**
     * 更新维护计划（管理员/老师）
     */
    @PutMapping("/plans/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> updatePlan(@PathVariable Long id, @RequestBody MaintenancePlan plan) {
        maintenanceService.updatePlan(id, plan);
        return Result.success("维护计划更新成功", null);
    }

    /**
     * 删除维护计划（管理员/老师）
     */
    @DeleteMapping("/plans/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> deletePlan(@PathVariable Long id) {
        maintenanceService.deletePlan(id);
        return Result.success("维护计划删除成功", null);
    }

    /**
     * 分配维护任务（管理员/老师）
     */
    @PostMapping("/plans/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> assignPlan(@PathVariable Long id, @RequestParam Long assigneeId) {
        maintenanceService.assignMaintenancePlan(id, assigneeId);
        return Result.success("维护任务分配成功", null);
    }

    /**
     * 获取分配给我的维护任务
     */
    @GetMapping("/my-plans")
    public Result<PageResult<MaintenancePlan>> getMyPlans(PageQueryDTO query,
                                                          @AuthenticationPrincipal CustomeUserDetails userDetails) {
        PageResult<MaintenancePlan> result = maintenanceService.getMyAssignedPlans(userDetails.getUserId(), query);
        return Result.success(result);
    }

    /**
     * 获取维护记录列表
     */
    @GetMapping("/records")
    public Result<PageResult<MaintenanceRecord>> getRecords(PageQueryDTO query) {
        PageResult<MaintenanceRecord> result = maintenanceService.getRecordList(query);
        return Result.success(result);
    }

    /**
     * 添加维护记录（管理员）
     */
    @PostMapping("/records")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> addRecord(@RequestBody MaintenanceRecord record) {
        Long id = maintenanceService.addRecord(record);
        return Result.success("维护记录添加成功", id);
    }

    /**
     * 完成维护（管理员/老师/被分配的学生）
     */
    @PostMapping("/plans/{planId}/complete")
    public Result<Void> completeMaintenance(@PathVariable Long planId,
                                             @RequestParam Long equipmentId,
                                             @RequestParam String content,
                                             @RequestParam String result,
                                             @AuthenticationPrincipal CustomeUserDetails userDetails) {
        maintenanceService.completeMaintenance(planId, equipmentId, content, result, userDetails.getUserId());
        return Result.success("维护已完成", null);
    }

    /**
     * 获取需要提醒的维护计划（管理员）
     */
    @GetMapping("/reminders")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<MaintenancePlan>> getReminders() {
        List<MaintenancePlan> reminders = maintenanceService.getPendingReminders();
        return Result.success(reminders);
    }

    /**
     * 获取可分配的老师列表（管理员/老师）
     */
    @GetMapping("/teachers")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<List<SysUser>> getTeachers() {
        List<SysUser> teachers = maintenanceService.getAllTeachers();
        return Result.success(teachers);
    }
}
