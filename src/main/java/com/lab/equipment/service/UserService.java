package com.lab.equipment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.equipment.dto.CreateUserDTO;
import com.lab.equipment.dto.ResetPasswordDTO;
import com.lab.equipment.dto.UpdateUserDTO;
import com.lab.equipment.dto.UserQueryDTO;
import com.lab.equipment.entity.SysUser;
import com.lab.equipment.mapper.SysUserMapper;
import com.lab.equipment.security.CustomeUserDetails;
import com.lab.equipment.util.PasswordUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户管理服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper userMapper;

    /**
     * 分页查询用户列表
     */
    public Page<SysUser> getUserList(UserQueryDTO queryDTO) {
        Page<SysUser> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        // 用户名模糊查询
        if (StringUtils.hasText(queryDTO.getUsername())) {
            wrapper.like(SysUser::getUsername, queryDTO.getUsername());
        }
        // 真实姓名模糊查询
        if (StringUtils.hasText(queryDTO.getRealName())) {
            wrapper.like(SysUser::getRealName, queryDTO.getRealName());
        }
        // 学号/工号模糊查询
        if (StringUtils.hasText(queryDTO.getStudentNo())) {
            wrapper.like(SysUser::getStudentNo, queryDTO.getStudentNo());
        }
        // 角色筛选
        if (StringUtils.hasText(queryDTO.getRole())) {
            wrapper.eq(SysUser::getRole, queryDTO.getRole());
        }
        // 院系模糊查询
        if (StringUtils.hasText(queryDTO.getDepartment())) {
            wrapper.like(SysUser::getDepartment, queryDTO.getDepartment());
        }
        // 状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, queryDTO.getStatus());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> resultPage = userMapper.selectPage(page, wrapper);
        // 清除密码
        resultPage.getRecords().forEach(user -> user.setPassword(null));
        return resultPage;
    }

    /**
     * 根据ID获取用户详情
     */
    public SysUser getUserById(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    /**
     * 创建新用户
     */
    @Transactional(rollbackFor = Exception.class)
    public SysUser createUser(CreateUserDTO dto) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查学号/工号是否已存在
        if (StringUtils.hasText(dto.getStudentNo())) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getStudentNo, dto.getStudentNo());
            if (userMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("学号/工号已存在");
            }
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordUtil.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setStudentNo(dto.getStudentNo());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());
        user.setDepartment(dto.getDepartment());
        user.setMajor(dto.getMajor());
        user.setAvatar(dto.getAvatar());
        user.setStatus(1); // 默认启用
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);
        user.setPassword(null); // 返回时清除密码
        return user;
    }

    /**
     * 更新用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public SysUser updateUser(UpdateUserDTO dto) {
        SysUser user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查学号/工号是否已被其他用户使用
        if (StringUtils.hasText(dto.getStudentNo())) {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getStudentNo, dto.getStudentNo());
            wrapper.ne(SysUser::getId, dto.getId());
            if (userMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("学号/工号已被其他用户使用");
            }
        }

        // 更新字段
        if (StringUtils.hasText(dto.getRealName())) {
            user.setRealName(dto.getRealName());
        }
        if (StringUtils.hasText(dto.getStudentNo()) || dto.getStudentNo() == null) {
            user.setStudentNo(dto.getStudentNo());
        }
        if (StringUtils.hasText(dto.getEmail()) || dto.getEmail() == null) {
            user.setEmail(dto.getEmail());
        }
        if (StringUtils.hasText(dto.getPhone()) || dto.getPhone() == null) {
            user.setPhone(dto.getPhone());
        }
        if (StringUtils.hasText(dto.getRole())) {
            user.setRole(dto.getRole());
        }
        if (StringUtils.hasText(dto.getDepartment()) || dto.getDepartment() == null) {
            user.setDepartment(dto.getDepartment());
        }
        if (StringUtils.hasText(dto.getMajor()) || dto.getMajor() == null) {
            user.setMajor(dto.getMajor());
        }
        if (StringUtils.hasText(dto.getAvatar()) || dto.getAvatar() == null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);
        user.setPassword(null); // 返回时清除密码
        return user;
    }

    /**
     * 删除用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        SysUser currentUser = getCurrentUser();
        if (currentUser.getId().equals(userId)) {
            throw new RuntimeException("不能删除自己的账号");
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        userMapper.deleteById(userId);
    }

    /**
     * 重置用户密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordDTO dto) {
        SysUser user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setPassword(PasswordUtil.encode(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 批量删除用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Long> userIds) {
        SysUser currentUser = getCurrentUser();
        if (userIds.contains(currentUser.getId())) {
            throw new RuntimeException("不能删除自己的账号");
        }

        userMapper.deleteBatchIds(userIds);
    }

    /**
     * 批量更新用户状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStatus(List<Long> userIds, Integer status) {
        SysUser currentUser = getCurrentUser();
        if (userIds.contains(currentUser.getId())) {
            throw new RuntimeException("不能修改自己的状态");
        }

        if (status != 0 && status != 1) {
            throw new RuntimeException("状态值不合法");
        }

        userIds.forEach(userId -> {
            SysUser user = new SysUser();
            user.setId(userId);
            user.setStatus(status);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        });
    }

    /**
     * 获取当前用户
     */
    private SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomeUserDetails) {
            return ((CustomeUserDetails) authentication.getPrincipal()).getUser();
        }
        throw new RuntimeException("用户未登录");
    }

    /**
     * 获取所有角色列表（用于下拉选项）
     */
    public List<String> getRoleList() {
        return List.of("ADMIN", "TEACHER", "STUDENT");
    }
}
