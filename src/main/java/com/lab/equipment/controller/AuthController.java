package com.lab.equipment.controller;

import com.lab.equipment.common.Result;
import com.lab.equipment.dto.ChangePasswordDTO;
import com.lab.equipment.dto.LoginDTO;
import com.lab.equipment.dto.LoginResultDTO;
import com.lab.equipment.entity.SysUser;
import com.lab.equipment.security.CustomeUserDetails;
import com.lab.equipment.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResultDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginResultDTO result = authService.login(loginDTO);
        return Result.success(result);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        authService.logout();
        return Result.success("登出成功", null);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<SysUser> getCurrentUser(@AuthenticationPrincipal CustomeUserDetails userDetails) {
        SysUser user = authService.getCurrentUser();
        // 清除密码
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        authService.changePassword(dto.getOldPassword(), dto.getNewPassword());
        return Result.success("密码修改成功", null);
    }
}
