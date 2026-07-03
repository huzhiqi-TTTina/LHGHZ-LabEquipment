package com.lab.equipment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备预约DTO
 */
@Data
public class ReservationDTO {

    private Long id;

    /**
     * 设备ID
     */
    @NotNull(message = "设备不能为空")
    private Long equipmentId;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    /**
     * 使用目的
     */
    private String purpose;
}
