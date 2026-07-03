package com.lab.equipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 设备DTO
 */
@Data
public class EquipmentDTO {

    private Long id;

    /**
     * 设备编号
     */
    @NotBlank(message = "设备编号不能为空")
    private String equipmentNo;

    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空")
    private String equipmentName;

    /**
     * 分类ID
     */
    @NotNull(message = "分类不能为空")
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
     * 规格
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
