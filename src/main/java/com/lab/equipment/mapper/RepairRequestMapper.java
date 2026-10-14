package com.lab.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.equipment.entity.RepairRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
/**
 * 故障报修Mapper接口
 */
@Mapper
public interface RepairRequestMapper extends BaseMapper<RepairRequest> {
    /**
     * 查找流水号
     */
    @Select("SELECT MAX(repair_no) FROM repair_request WHERE repair_no LIKE CONCAT(#{prefix}, '%')")
    String selectMaxNoByPrefix(@Param("prefix") String prefix);
}
