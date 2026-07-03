package com.lab.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实验室实体类
 */
@Data
@TableName("laboratory")
public class Laboratory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 实验室ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 实验室名称
     */
    private String labName;

    /**
     * 实验室编码
     */
    private String labCode;

    /**
     * 位置
     */
    private String location;

    /**
     * 容纳人数
     */
    private Integer capacity;

    /**
     * 管理员ID
     */
    private Long managerId;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态：0-关闭，1-开放
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
