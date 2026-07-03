package com.lab.equipment.security;

import com.lab.equipment.entity.SysUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert; // 引入 Spring 的工具类，用于断言

import java.util.Collection;
import java.util.Collections;

/**
 * 自定义用户详情类
 */
@Getter
public class CustomeUserDetails implements UserDetails {

    private final SysUser user;

    public CustomeUserDetails(SysUser user) {
        // ⚠️ 关键修复 1: 构造函数强制检查 user 不能为 null
        Assert.notNull(user, "SysUser object cannot be null");
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ⚠️ 防止 user.getRole() 为 null 导致报错
        String role = user.getRole() != null ? user.getRole() : "USER";
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // ⚠️ 防止 user.getStatus() 为 null
        return user.getStatus() != null && user.getStatus() == 1;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // ⚠️ 防止 user.getStatus() 为 null
        return user.getStatus() != null && user.getStatus() == 1;
    }

    public Long getUserId() {
        // ⚠️ 关键修复 2: 安全获取 ID
        return user != null ? user.getId() : null;
    }

    public String getRealName() {
        return user != null ? user.getRealName() : null;
    }

    public String getRole() {
        return user != null ? user.getRole() : null;
    }
}