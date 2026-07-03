package com.lab.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备维护计划实体类
 */
@Data
@TableName("maintenance_plan")
public class MaintenancePlan implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 维护计划ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 计划名称
     */
    private String planName;

    /**
     * 设备ID，为空表示适用所有设备
     */
    private Long equipmentId;

    /**
     * 设备分类ID，为空表示适用所有设备
     */
    private Long categoryId;

    /**
     * 维护类型：DAILY-日常，WEEKLY-每周，MONTHLY-每月，QUARTERLY-每季度，YEARLY-每年
     */
    private String maintenanceType;

    /**
     * 维护周期（天数）
     */
    private Integer maintenanceCycle;

    /**
     * 下次维护日期
     */
    private LocalDate nextMaintenanceDate;

    /**
     * 维护说明
     */
    private String description;

    /**
     * 负责人ID
     */
    private Long responsibleUserId;

    /**
     * 最后完成日期
     */
    private LocalDate lastCompletedDate;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Integer isActive;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
