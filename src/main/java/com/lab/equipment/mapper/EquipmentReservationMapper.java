package com.lab.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.equipment.entity.EquipmentReservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备预约Mapper接口
 */
@Mapper
public interface EquipmentReservationMapper extends BaseMapper<EquipmentReservation> {

    /**
     * 查询设备预约冲突的记录
     */
    @Select("SELECT * FROM equipment_reservation WHERE equipment_id = #{equipmentId} " +
            "AND status IN ('PENDING', 'APPROVED') " +
            "AND ((start_time <= #{endTime} AND end_time >= #{startTime}))")
    List<EquipmentReservation> findConflictingReservations(
            @Param("equipmentId") Long equipmentId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
