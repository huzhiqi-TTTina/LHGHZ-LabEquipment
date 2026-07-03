package com.lab.equipment.dto;

import lombok.Data;

/**
 * 用户查询请求DTO
 */
@Data
public class UserQueryDTO {

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer size = 10;

    /**
     * 用户名（模糊查询）
     */
    private String username;

    /**
     * 真实姓名（模糊查询）
     */
    private String realName;

    /**
     * 学号/工号（模糊查询）
     */
    private String studentNo;

    /**
     * 角色筛选
     */
    private String role;

    /**
     * 院系（模糊查询）
     */
    private String department;

    /**
     * 状态筛选
     */
    private Integer status;
}
