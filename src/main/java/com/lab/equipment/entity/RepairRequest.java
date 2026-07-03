package com.lab.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 故障报修实体类
 */
@Data
@TableName("repair_request")
public class RepairRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报修ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报修编号
     */
    private String repairNo;

    /**
     * 设备ID
     */
    private Long equipmentId;

    /**
     * 报修人ID
     */
    private Long reportUserId;

    /**
     * 故障描述
     */
    private String faultDescription;

    /**
     * 故障图片URL，多个用逗号分隔
     */
    private String faultImageUrls;

    /**
     * 报修时间
     */
    private LocalDateTime reportTime;

    /**
     * 状态：PENDING-待处理，PROCESSING-处理中，COMPLETED-已完成，REJECTED-已驳回
     */
    private String status;

    /**
     * 优先级：LOW-低，NORMAL-中，HIGH-高，URGENT-紧急
     */
    private String priority;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 维修描述
     */
    private String repairDescription;

    /**
     * 维修费用
     */
    private BigDecimal repairCost;

    /**
     * 使用配件
     */
    private String partsUsed;

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
