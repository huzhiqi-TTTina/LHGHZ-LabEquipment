package com.lab.equipment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备视图对象
 */
@Data
public class EquipmentVO {

    private Long id;
    private String equipmentNo;
    private String equipmentName;
    private String uniqueCode;
    private Long categoryId;
    private String categoryName;
    private Long laboratoryId;
    private String laboratoryName;
    private String brand;
    private String model;
    private String serialNumber;
    private String qrCode;
    private String qrCodeImage;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private String supplier;
    private LocalDate warrantyDate;
    private String status;
    private String statusText;
    private String condition;
    private String specifications;
    private String description;
    private String imageUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
