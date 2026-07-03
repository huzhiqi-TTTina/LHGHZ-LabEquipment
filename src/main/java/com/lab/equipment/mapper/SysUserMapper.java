package com.lab.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.equipment.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
