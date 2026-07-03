package com.lab.equipment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建用户请求DTO
 */
@Data
public class CreateUserDTO {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String password;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /**
     * 学号/工号
     */
    private String studentNo;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 电话
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 角色：ADMIN-管理员，TEACHER-教师，STUDENT-学生
     */
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(ADMIN|TEACHER|STUDENT)$", message = "角色必须是ADMIN、TEACHER或STUDENT")
    private String role;

    /**
     * 院系
     */
    private String department;

    /**
     * 专业
     */
    private String major;

    /**
     * 头像URL
     */
    private String avatar;
}
