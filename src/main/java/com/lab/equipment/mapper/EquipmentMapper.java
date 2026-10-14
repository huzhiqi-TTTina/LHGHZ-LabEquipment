package com.lab.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.equipment.dto.UtilizationTrendDTO;
import com.lab.equipment.entity.Equipment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 设备Mapper接口
 */
@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {

    /**
     * 统计设备数量（按状态）
     */
    @Select("SELECT COUNT(*) FROM equipment WHERE status = #{status}")
    Long countByStatus(@Param("status") String status);

    /**
     * 统计设备数量（按分类）
     */
    @Select("SELECT COUNT(*) FROM equipment WHERE category_id = #{categoryId}")
    Long countByCategory(@Param("categoryId") Long categoryId);

    /**
     * 获取设备利用率统计
     */
    @Select("SELECT COUNT(*) FROM equipment WHERE status IN ('BORROWED', 'NORMAL')")
    Long getAvailableCount();
    /**
     * 获取最近N天是设备利用率
     */
    @Select("""
        SELECT 
            DATE_FORMAT(update_time, '%Y-%m-%d') AS date,
            ROUND(
                SUM(CASE WHEN status = 'BORROWED' THEN 1 ELSE 0 END) * 100.0 / 
                NULLIF(SUM(CASE WHEN status != 'SCRAPPED' THEN 1 ELSE 0 END), 0), 
            1) AS utilizationRate
        FROM equipment
        WHERE update_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY)
        GROUP BY DATE_FORMAT(update_time, '%Y-%m-%d')
        ORDER BY date ASC
    """)
    List<UtilizationTrendDTO> selectUtilizationTrend(@Param("days")Integer days);
}
