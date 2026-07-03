package com.lab.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.equipment.entity.Equipment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
