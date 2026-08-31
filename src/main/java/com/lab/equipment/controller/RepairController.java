package com.lab.equipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.equipment.common.PageResult;
import com.lab.equipment.common.Result;
import com.lab.equipment.dto.PageQueryDTO;
import com.lab.equipment.dto.RepairRequestDTO;
import com.lab.equipment.dto.TaskStatsVO;
import com.lab.equipment.entity.RepairRequest;
import com.lab.equipment.entity.SysUser;
import com.lab.equipment.mapper.SysUserMapper;
import com.lab.equipment.security.CustomeUserDetails;
import com.lab.equipment.service.RepairService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 故障报修控制器
 */
@RestController
@RequestMapping("/repair")
@RequiredArgsConstructor
public class RepairController {

    private final RepairService repairService;

    /**
     * 分页查询报修列表
     */
    @GetMapping("/list")
    public Result<PageResult<RepairRequest>> list(PageQueryDTO query) {
        PageResult<RepairRequest> result = repairService.getRepairList(query);
        return Result.success(result);
    }

    /**
     * 获取可分配的老师列表（管理员/老师）
     */
    @GetMapping("/teachers")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<List<SysUser>> getTeachers() {
        List<SysUser> teachers = repairService.getAllTeachers();
        return Result.success(teachers);
    }


    /**
     * 提交报修
     */
    @PostMapping
    public Result<Long> submit(@Valid @RequestBody RepairRequestDTO dto,
                               @AuthenticationPrincipal CustomeUserDetails userDetails) {
        Long id = repairService.submitRepair(userDetails.getUserId(), dto);
        return Result.success("报修提交成功", id);
    }

    /**
     * 处理报修（管理员/老师）
     */
    @PostMapping("/{id}/handle")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> handle(@PathVariable Long id,
                               @AuthenticationPrincipal CustomeUserDetails userDetails) {
        repairService.handleRepair(id, userDetails.getUserId());
        return Result.success("报修已开始处理", null);
    }

    /**
     * 分配报修任务（管理员/老师）
     */
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> assign(@PathVariable Long id,
                               @RequestParam Long assigneeId) {
        repairService.assignRepair(id, assigneeId);
        return Result.success("任务分配成功", null);
    }

    /**
     * 获取分配给我的报修任务
     */
    @GetMapping("/my-tasks")
    public Result<PageResult<RepairRequest>> getMyTasks(PageQueryDTO query,
                                                        @AuthenticationPrincipal CustomeUserDetails userDetails) {
        PageResult<RepairRequest> result = repairService.getMyAssignedRepairs(userDetails.getUserId(), query);
        return Result.success(result);
    }

    /**
     * 完成报修（管理员/老师/被分配的学生）
     */
    @PostMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id,
                                 @RequestParam String repairDescription,
                                 @RequestParam(required = false) String partsUsed,
                                 @RequestParam(required = false) Double repairCost,
                                 @AuthenticationPrincipal CustomeUserDetails userDetails) {
        repairService.completeRepair(id, userDetails.getUserId(), repairDescription, partsUsed, repairCost);
        return Result.success("报修已完成", null);
    }

    /**
     * 驳回报修（管理员）
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> reject(@PathVariable Long id,
                               @RequestParam(required = false) String remark) {
        repairService.rejectRepair(id, remark);
        return Result.success("报修已驳回", null);
    }

    /**
     * 获取待处理报修数量
     */
    @GetMapping("/pending-count")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> getPendingCount() {
        Long count = repairService.getPendingRepairCount();
        return Result.success(count);
    }
    /**
     * 根据ID获取报修详情
     */
    @GetMapping("/{id}")
    public Result<RepairRequest> getById(@PathVariable Long id) {
        RepairRequest repair = repairService.getRepairById(id);
        return Result.success(repair);
    }


    /**
     * 获取报修任务分配统计（管理员）
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<TaskStatsVO> getStats() {
        TaskStatsVO stats = repairService.getRepairTaskStats();
        return Result.success(stats);
    }
}