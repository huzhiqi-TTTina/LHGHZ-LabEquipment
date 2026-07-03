package com.lab.equipment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.equipment.common.Result;
import com.lab.equipment.dto.CreateUserDTO;
import com.lab.equipment.dto.ResetPasswordDTO;
import com.lab.equipment.dto.UpdateUserDTO;
import com.lab.equipment.dto.UserQueryDTO;
import com.lab.equipment.entity.SysUser;
import com.lab.equipment.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class SysUserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<SysUser>> getUserList(UserQueryDTO queryDTO) {
        Page<SysUser> page = userService.getUserList(queryDTO);
        return Result.success(page);
    }

    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysUser> getUserById(@PathVariable Long userId) {
        SysUser user = userService.getUserById(userId);
        return Result.success(user);
    }

    /**
     * 创建新用户
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysUser> createUser(@Valid @RequestBody CreateUserDTO dto) {
        SysUser user = userService.createUser(dto);
        return Result.success("用户创建成功", user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysUser> updateUser(@Valid @RequestBody UpdateUserDTO dto) {
        SysUser user = userService.updateUser(dto);
        return Result.success("用户更新成功", user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return Result.success("用户删除成功", null);
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> batchDeleteUsers(@RequestBody List<Long> userIds) {
        userService.batchDeleteUsers(userIds);
        return Result.success("批量删除成功", null);
    }

    /**
     * 重置用户密码
     */
    @PostMapping("/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(dto);
        return Result.success("密码重置成功", null);
    }

    /**
     * 批量更新用户状态
     */
    @PutMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> batchUpdateStatus(@RequestBody List<Long> userIds, @RequestParam Integer status) {
        userService.batchUpdateStatus(userIds, status);
        return Result.success("状态更新成功", null);
    }

    /**
     * 获取所有角色列表
     */
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<String>> getRoleList() {
        List<String> roles = userService.getRoleList();
        return Result.success(roles);
    }

    /**
     * 获取学生列表（用于任务分配，管理员和老师可访问）
     */
    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Page<SysUser>> getStudentList(UserQueryDTO queryDTO) {
        queryDTO.setRole("STUDENT");
        queryDTO.setSize(1000);
        Page<SysUser> page = userService.getUserList(queryDTO);
        return Result.success(page);
    }
}
