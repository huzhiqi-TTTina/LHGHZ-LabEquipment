package com.lab.equipment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统计数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDTO {

    /**
     * 设备总数
     */
    private Long totalEquipment;

    /**
     * 正常设备数
     */
    private Long normalEquipment;

    /**
     * 借出设备数
     */
    private Long borrowedEquipment;

    /**
     * 维修中设备数
     */
    private Long maintenanceEquipment;

    /**
     * 报废设备数
     */
    private Long scrappedEquipment;

    /**
     * 今日借用次数
     */
    private Long todayBorrowCount;

    /**
     * 本月借用次数
     */
    private Long monthBorrowCount;

    /**
     * 待审核预约数
     */
    private Long pendingApprovalCount;

    /**
     * 待处理报修数
     */
    private Long pendingRepairCount;

    /**
     * 设备利用率
     */
    private Double utilizationRate;

    /**
     * 设备损坏率
     */
    private Double damageRate;
}
