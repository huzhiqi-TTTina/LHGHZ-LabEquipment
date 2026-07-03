package com.lab.equipment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.equipment.dto.LoginDTO;
import com.lab.equipment.dto.LoginResultDTO;
import com.lab.equipment.entity.SysUser;
import com.lab.equipment.mapper.SysUserMapper;
import com.lab.equipment.security.CustomeUserDetails;
import com.lab.equipment.util.JwtUtil;
import com.lab.equipment.util.PasswordUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final SysUserMapper userMapper;

    /**
     * 用户登录
     */
    public LoginResultDTO login(LoginDTO loginDTO) {
        try {
            // 2. 手动比对密码 (使用你注入的 passwordEncoder)
            SysUser user1 = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, loginDTO.getUsername()));
            if (user1 == null) {
                throw new RuntimeException("用户不存在");
            }
            System.out.println(">>> 前端传的密码: " + loginDTO.getPassword());
            System.out.println(">>> 数据库存的密码: " + user1.getPassword());

            // 认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsername(),
                            loginDTO.getPassword()
                    )
            );


            // 获取用户信息
            CustomeUserDetails userDetails = (CustomeUserDetails) authentication.getPrincipal();
            SysUser user = userDetails.getUser();

            // 生成Token
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

            // 返回结果
            return LoginResultDTO.builder()
                    .token(token)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .role(user.getRole())
                    .avatar(user.getAvatar())
                    .build();

        } catch (AuthenticationException e) {
            throw new RuntimeException("用户名或密码错误");
        }
    }

    /**
     * 用户登出
     */
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 获取当前用户ID
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomeUserDetails) {
            return ((CustomeUserDetails) authentication.getPrincipal()).getUserId();
        }
        return null;
    }

    /**
     * 获取当前用户
     */
    public SysUser getCurrentUser() {
        Long userId = getCurrentUserId();
        if (userId != null) {
            return userMapper.selectById(userId);
        }
        return null;
    }

    /**
     * 修改密码
     */
    public void changePassword(String oldPassword, String newPassword) {
        SysUser currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }

        // 验证旧密码
        if (!PasswordUtil.matches(oldPassword, currentUser.getPassword())) {
            throw new RuntimeException("旧密码不正确");
        }

        // 更新密码
        currentUser.setPassword(PasswordUtil.encode(newPassword));
        userMapper.updateById(currentUser);
    }
}
