package com.lab.equipment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * H5设备信息VO
 */
@Data
public class H5EquipmentVO {

    /**
     * 设备ID
     */
    private Long id;

    /**
     * 设备编号
     */
    private String equipmentNo;

    /**
     * 设备名称
     */
    private String equipmentName;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 所属实验室名称
     */
    private String laboratoryName;

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
     * 状态：NORMAL-正常，BORROWED-借出，MAINTENANCE-维修中，SCRAPPED-报废
     */
    private String status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 完好程度：GOOD-完好，DAMAGED-损坏，SERIOUS-严重损坏
     */
    private String equipmentCondition;

    /**
     * 完好程度文本
     */
    private String conditionText;

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
}
