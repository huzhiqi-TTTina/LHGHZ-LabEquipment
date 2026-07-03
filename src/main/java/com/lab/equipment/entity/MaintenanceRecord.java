package com.lab.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备维护记录实体类
 */
@Data
@TableName("maintenance_record")
public class MaintenanceRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 维护记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 记录编号
     */
    private String recordNo;

    /**
     * 维护计划ID
     */
    private Long planId;

    /**
     * 计划名称（非数据库字段，用于显示）
     */
    @TableField(exist = false)
    private String planName;

    /**
     * 设备ID
     */
    private Long equipmentId;

    /**
     * 设备名称（非数据库字段，用于显示）
     */
    @TableField(exist = false)
    private String equipmentName;

    /**
     * 维护时间
     */
    private LocalDateTime maintenanceDate;

    /**
     * 维护类型
     */
    private String maintenanceType;

    /**
     * 维护内容
     */
    private String maintenanceContent;

    /**
     * 维护结果：SUCCESS-成功，FAILED-失败，PARTIAL-部分完成
     */
    private String maintenanceResult;

    /**
     * 维护人ID
     */
    private Long maintainerId;

    /**
     * 维护时长（分钟）
     */
    private Integer maintenanceDuration;

    /**
     * 维护费用
     */
    private BigDecimal cost;

    /**
     * 使用配件
     */
    private String partsUsed;

    /**
     * 下次维护日期
     */
    private LocalDate nextMaintenanceDate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
