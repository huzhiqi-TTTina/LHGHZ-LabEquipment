package com.lab.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备二维码图片（二进制存库，不依赖本地磁盘）
 */
@Data
@TableName("equipment_qr_image")
public class EquipmentQrImage {

    @TableId(type = IdType.INPUT)
    private Long equipmentId;

    private String contentType;

    private byte[] imageData;

    private LocalDateTime updatedAt;
}
