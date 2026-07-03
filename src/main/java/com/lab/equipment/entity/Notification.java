package com.lab.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知实体类
 */
@Data
@TableName("notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 通知类型：SYSTEM-系统，APPROVE-审核，MAINTENANCE-维护，OVERDUE-逾期
     */
    private String type;

    /**
     * 接收人ID
     */
    private Long recipientId;

    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 关联ID（如预约ID、报修ID等）
     */
    private Long relatedId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
