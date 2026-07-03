package com.lab.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.equipment.entity.RepairRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 故障报修Mapper接口
 */
@Mapper
public interface RepairRequestMapper extends BaseMapper<RepairRequest> {
}
