package com.lab.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.equipment.entity.MaintenanceRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 维护记录Mapper接口
 */
@Mapper
public interface MaintenanceRecordMapper extends BaseMapper<MaintenanceRecord> {
}
