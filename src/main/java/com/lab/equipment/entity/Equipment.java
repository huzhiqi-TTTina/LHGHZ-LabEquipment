package com.lab.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备实体类
 */
@Data
@TableName("equipment")
public class Equipment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备编号
     */
    private String equipmentNo;

    /**
     * 设备唯一标识码（用于扫码）
     */
    private String uniqueCode;

    /**
     * 设备名称
     */
    private String equipmentName;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 所属实验室ID
     */
    private Long laboratoryId;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 型号
     */
    private String model;

    /**
     * 序列号
     */
    private String serialNumber;

    /**
     * 二维码内容
     */
    private String qrCode;

    /**
     * 二维码图片URL
     */
    private String qrCodeImage;

    /**
     * 购买日期
     */
    private LocalDate purchaseDate;

    /**
     * 购买价格
     */
    private BigDecimal purchasePrice;

    /**
     * 供应商
     */
    private String supplier;

    /**
     * 保修到期日期
     */
    private LocalDate warrantyDate;

    /**
     * 状态：NORMAL-正常，BORROWED-借出，MAINTENANCE-维修中，SCRAPPED-报废
     */
    private String status;

    /**
     * 完好程度：GOOD-完好，DAMAGED-损坏，SERIOUS-严重损坏
     */
    @Getter
    @Setter
    private String equipmentCondition;

    /**
     * 规格参数
     */
    private String specifications;

    /**
     * 描述
     */
    private String description;

    /**
     * 设备图片URL
     */
    private String imageUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
