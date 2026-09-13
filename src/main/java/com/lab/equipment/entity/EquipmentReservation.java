package com.lab.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备预约实体类
 */
@Data
@TableName("equipment_reservation")
public class EquipmentReservation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 预约ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 预约编号
     */
    private String reservationNo;

    /**
     * 设备ID
     */
    private Long equipmentId;
    /**
     * 预约人姓名
     */
    @TableField(exist = false)
    private String userName;
    /**
     * 预约人ID
     */
    private Long userId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 使用目的
     */
    private String purpose;

    /**
     * 状态：PENDING-待审核，APPROVED-已批准，REJECTED-已拒绝，CANCELLED-已取消，COMPLETED-已完成
     */
    private String status;

    /**
     * 审核人ID
     */
    private Long approveUserId;

    /**
     * 审核时间
     */
    private LocalDateTime approveTime;

    /**
     * 审核备注
     */
    private String approveRemark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
