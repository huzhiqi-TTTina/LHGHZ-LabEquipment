package com.lab.equipment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.equipment.common.PageResult;
import com.lab.equipment.dto.PageQueryDTO;
import com.lab.equipment.dto.ReservationDTO;
import com.lab.equipment.entity.Equipment;
import com.lab.equipment.entity.EquipmentReservation;
import com.lab.equipment.entity.SysUser;
import com.lab.equipment.enums.EquipmentStatus;
import com.lab.equipment.enums.ReservationStatus;
import com.lab.equipment.mapper.EquipmentMapper;
import com.lab.equipment.mapper.EquipmentReservationMapper;
import com.lab.equipment.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 设备预约服务
 */
@Service
@RequiredArgsConstructor
public class ReservationService extends ServiceImpl<EquipmentReservationMapper, EquipmentReservation> {
    private final EquipmentReservationMapper reservationMapper;
    private final EquipmentMapper equipmentMapper;
    private final SysUserMapper userMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 分页查询预约列表
     */
    /**public PageResult<EquipmentReservation> getReservationList(PageQueryDTO query) {
        LambdaQueryWrapper<EquipmentReservation> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.eq(EquipmentReservation::getReservationNo, query.getKeyword()));
        }

        wrapper.orderByDesc(EquipmentReservation::getCreateTime);

        Page<EquipmentReservation> page = reservationMapper.selectPage(
                new Page<>(query.getCurrent(), query.getSize()),
                wrapper
        );

        return PageResult.of(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }
   */
    public PageResult<EquipmentReservation> getReservationList(PageQueryDTO query) {
        // 1. 构建查询条件
        LambdaQueryWrapper<EquipmentReservation> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.eq(EquipmentReservation::getReservationNo, query.getKeyword()));
        }

        wrapper.orderByDesc(EquipmentReservation::getCreateTime);

        // 2. 分页查询预约记录（此时 userName 是空的）
        Page<EquipmentReservation> page = reservationMapper.selectPage(
                new Page<>(query.getCurrent(), query.getSize()),
                wrapper
        );

        // 3. 遍历当前页的数据，查名字并塞进去
        List<EquipmentReservation> records = page.getRecords();
        for (EquipmentReservation record : records) {
            if (record.getUserId() != null) {
                // 这里调用你项目里的 SysUserMapper 去查用户
                // 假设 SysUser 实体类里，存名字的字段是 realName
                SysUser user = sysUserMapper.selectById(record.getUserId());
                if (user != null) {
                    record.setUserName(user.getRealName());
                }
            }
        }

        // 4. 返回结果
        return PageResult.of(page.getTotal(), records, page.getCurrent(), page.getSize());
    }
    /**
     * 获取我的预约列表
     */
    public PageResult<EquipmentReservation> getMyReservationList(Long userId, PageQueryDTO query) {
        LambdaQueryWrapper<EquipmentReservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EquipmentReservation::getUserId, userId);
        wrapper.orderByDesc(EquipmentReservation::getCreateTime);

        Page<EquipmentReservation> page = reservationMapper.selectPage(
                new Page<>(query.getCurrent(), query.getSize()),
                wrapper
        );

        return PageResult.of(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }

    /**
     * 根据ID获取预约详情
     */
    public EquipmentReservation getReservationById(Long id) {
        EquipmentReservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }
        return reservation;
    }

    /**
     * 创建预约
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createReservation(Long userId, ReservationDTO dto) {
        // 检查设备是否存在且可用
        Equipment equipment = equipmentMapper.selectById(dto.getEquipmentId());
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }

        if (!EquipmentStatus.NORMAL.name().equals(equipment.getStatus())) {
            throw new RuntimeException("设备当前状态不可预约");
        }

        // 检查时间冲突
        List<EquipmentReservation> conflicts = reservationMapper.findConflictingReservations(
                dto.getEquipmentId(),
                dto.getStartTime(),
                dto.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("该时间段已被预约");
        }

        // 检查时间范围
        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new RuntimeException("结束时间不能早于开始时间");
        }

        if (dto.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("预约开始时间不能早于当前时间");
        }

        // 创建预约
        EquipmentReservation reservation = new EquipmentReservation();
        BeanUtils.copyProperties(dto, reservation);
        reservation.setUserId(userId);
        reservation.setReservationNo(generateReservationNo());
        reservation.setStatus(ReservationStatus.PENDING.name());
        reservationMapper.insert(reservation);

        return reservation.getId();
    }

    /**
     * 审核预约
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveReservation(Long id, String status, String remark, Long approveUserId) {
        EquipmentReservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }

        if (!ReservationStatus.PENDING.name().equals(reservation.getStatus())) {
            throw new RuntimeException("预约已审核，不能重复操作");
        }

        // 验证状态
        if (!ReservationStatus.APPROVED.name().equals(status)
                && !ReservationStatus.REJECTED.name().equals(status)) {
            throw new RuntimeException("无效的审核状态");
        }

        reservation.setStatus(status);
        reservation.setApproveUserId(approveUserId);
        reservation.setApproveTime(LocalDateTime.now());
        reservation.setApproveRemark(remark);
        reservationMapper.updateById(reservation);

        // 如果拒绝，更新设备状态为正常
        if (ReservationStatus.REJECTED.name().equals(status)) {
            // 发送通知（省略）
        }
    }

    /**
     * 取消预约
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelReservation(Long id, Long userId) {
        EquipmentReservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }

        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("只能取消自己的预约");
        }

        if (!ReservationStatus.PENDING.name().equals(reservation.getStatus())) {
            throw new RuntimeException("预约已审核，不能取消");
        }

        reservation.setStatus(ReservationStatus.CANCELLED.name());
        reservationMapper.updateById(reservation);
    }

    /**
     * 生成预约编号
     */
    private String generateReservationNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomNum = ThreadLocalRandom.current().nextInt(100, 1000);
        return "RES" + dateStr + randomNum;//末尾不是流水号，需要改动
    }
}
