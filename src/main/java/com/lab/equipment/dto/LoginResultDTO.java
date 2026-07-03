package com.lab.equipment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResultDTO {

    /**
     * Token
     */
    private String token;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 角色
     */
    private String role;

    /**
     * 头像
     */
    private String avatar;
}
