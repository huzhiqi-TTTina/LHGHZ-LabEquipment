package com.lab.equipment.entity; // 注意改成您的实际包名

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("equipment_daily_stats")
public class EquipmentDailyStats {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate statsDate;       // 统计日期
    private Integer totalEquipment;    // 有效总数
    private Integer borrowedEquipment; // 借出数
    private Double utilizationRate;    // 利用率
    private LocalDateTime createTime;  // 创建时间
}
