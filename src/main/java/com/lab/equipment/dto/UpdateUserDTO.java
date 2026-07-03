package com.lab.equipment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新用户请求DTO
 */
@Data
public class UpdateUserDTO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 真实姓名
     */
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

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}
