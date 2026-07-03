package com.lab.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.equipment.entity.Laboratory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 实验室Mapper接口
 */
@Mapper
public interface LaboratoryMapper extends BaseMapper<Laboratory> {
}
