package com.lab.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.equipment.entity.MaintenancePlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 维护计划Mapper接口
 */
@Mapper
public interface MaintenancePlanMapper extends BaseMapper<MaintenancePlan> {

    /**
     * 查询需要提醒的维护计划
     */
    @Select("SELECT * FROM maintenance_plan WHERE is_active = 1 AND next_maintenance_date <= #{date}")
    List<MaintenancePlan> findPendingReminders(@Param("date") LocalDate date);
}
