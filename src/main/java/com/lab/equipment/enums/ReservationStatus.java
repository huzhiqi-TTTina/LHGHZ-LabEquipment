package com.lab.equipment.enums;

/**
 * 预约状态枚举
 */
public enum ReservationStatus {
    /**
     * 待审核
     */
    PENDING,
    /**
     * 已批准
     */
    APPROVED,
    /**
     * 已拒绝
     */
    REJECTED,
    /**
     * 已取消
     */
    CANCELLED,
    /**
     * 已完成
     */
    COMPLETED
}
