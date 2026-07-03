package com.lab.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.equipment.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 借用记录Mapper接口
 */
@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {

    /**
     * 查询逾期未归还的记录
     */
    @Select("SELECT * FROM borrow_record WHERE borrow_status = 'BORROWED' AND expected_return_date < #{now}")
    List<BorrowRecord> findOverdueRecords(@Param("now") LocalDateTime now);

    /**
     * 统计用户借用次数
     */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE user_id = #{userId}")
    Long countByUserId(@Param("userId") Long userId);
}
