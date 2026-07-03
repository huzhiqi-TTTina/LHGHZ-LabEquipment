package com.lab.equipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 故障报修DTO
 */
@Data
public class RepairRequestDTO {

    private Long id;

    /**
     * 设备ID
     */
    @NotNull(message = "设备不能为空")
    private Long equipmentId;

    /**
     * 故障描述
     */
    @NotBlank(message = "故障描述不能为空")
    private String faultDescription;

    /**
     * 故障图片URL列表
     */
    private String faultImageUrls;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 备注
     */
    private String remark;
}
