package com.lab.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备借用记录实体类
 */
@Data
@TableName("borrow_record")
public class BorrowRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 借出记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 借出编号
     */
    private String borrowNo;

    /**
     * 设备ID
     */
    private Long equipmentId;

    /**
     * 借用人ID
     */
    private Long userId;

    /**
     * 借出时间
     */
    private LocalDateTime borrowDate;

    /**
     * 预计归还时间
     */
    private LocalDateTime expectedReturnDate;

    /**
     * 实际归还时间
     */
    private LocalDateTime actualReturnDate;

    /**
     * 借用用途
     */
    private String borrowPurpose;

    /**
     * 状态：BORROWED-借出中，RETURNED-已归还，OVERDUE-逾期
     */
    private String borrowStatus;

    /**
     * 处理人ID（管理员）
     */
    private Long handlerId;

    /**
     * 归还处理人ID
     */
    private Long returnHandlerId;

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
