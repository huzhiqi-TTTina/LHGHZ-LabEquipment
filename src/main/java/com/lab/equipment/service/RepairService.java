package com.lab.equipment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.equipment.common.PageResult;
import com.lab.equipment.dto.PageQueryDTO;
import com.lab.equipment.dto.RepairRequestDTO;
import com.lab.equipment.dto.TaskStatsVO;
import com.lab.equipment.entity.Equipment;
import com.lab.equipment.entity.RepairRequest;
import com.lab.equipment.entity.SysUser;
import com.lab.equipment.enums.RepairStatus;
import com.lab.equipment.mapper.EquipmentMapper;
import com.lab.equipment.mapper.RepairRequestMapper;
import com.lab.equipment.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 故障报修服务
 */
@Service
@RequiredArgsConstructor
public class RepairService extends ServiceImpl<RepairRequestMapper, RepairRequest> {

    private final RepairRequestMapper repairRequestMapper;
    private final EquipmentMapper equipmentMapper;
    private final SysUserMapper userMapper;

    /**
     * 分页查询报修列表
     */
    public PageResult<RepairRequest> getRepairList(PageQueryDTO query) {
        LambdaQueryWrapper<RepairRequest> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.eq(RepairRequest::getRepairNo, query.getKeyword()));
        }

        wrapper.orderByDesc(RepairRequest::getReportTime);

        Page<RepairRequest> page = repairRequestMapper.selectPage(
                new Page<>(query.getCurrent(), query.getSize()),
                wrapper
        );

        return PageResult.of(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }

    /**
     * 根据ID获取报修详情
     */
    public RepairRequest getRepairById(Long id) {
        RepairRequest repair = repairRequestMapper.selectById(id);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }
        return repair;
    }

    /**
     * 提交报修
     */
    @Transactional(rollbackFor = Exception.class)
    public Long submitRepair(Long userId, RepairRequestDTO dto) {
        // 检查设备是否存在
        Equipment equipment = equipmentMapper.selectById(dto.getEquipmentId());
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }

        // 创建报修记录
        RepairRequest repair = new RepairRequest();
        BeanUtils.copyProperties(dto, repair);
        repair.setReportUserId(userId);
        repair.setRepairNo(generateRepairNo());
        repair.setReportTime(LocalDateTime.now());
        repair.setStatus(RepairStatus.PENDING.name());
        repair.setPriority(dto.getPriority() != null ? dto.getPriority() : "NORMAL");
        repairRequestMapper.insert(repair);

        // 更新设备状态为维修中
        equipment.setStatus("MAINTENANCE");
        equipmentMapper.updateById(equipment);

        // 发送通知给管理员（省略）

        return repair.getId();
    }

    /**
     * 处理报修
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleRepair(Long id, Long handlerId) {
        RepairRequest repair = repairRequestMapper.selectById(id);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }

        if (!RepairStatus.PENDING.name().equals(repair.getStatus())) {
            throw new RuntimeException("报修记录已处理");
        }

        repair.setStatus(RepairStatus.PROCESSING.name());
        repair.setHandlerId(handlerId);
        repair.setHandleTime(LocalDateTime.now());
        repairRequestMapper.updateById(repair);
    }

    /**
     * 分配报修任务给指定用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRepair(Long id, Long assigneeId) {
        RepairRequest repair = repairRequestMapper.selectById(id);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }

        if (!RepairStatus.PENDING.name().equals(repair.getStatus())) {
            throw new RuntimeException("只能分配待处理的报修任务");
        }

        repair.setHandlerId(assigneeId);
        repair.setStatus(RepairStatus.PROCESSING.name());
        repair.setHandleTime(LocalDateTime.now());
        repairRequestMapper.updateById(repair);
    }

    /**
     * 获取分配给我的报修任务
     */
    public PageResult<RepairRequest> getMyAssignedRepairs(Long userId, PageQueryDTO query) {
        LambdaQueryWrapper<RepairRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairRequest::getHandlerId, userId);
        wrapper.in(RepairRequest::getStatus, RepairStatus.PROCESSING.name(), RepairStatus.PENDING.name());
        wrapper.orderByDesc(RepairRequest::getReportTime);

        Page<RepairRequest> page = repairRequestMapper.selectPage(
                new Page<>(query.getCurrent(), query.getSize()),
                wrapper
        );

        return PageResult.of(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }

    /**
     * 完成报修
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeRepair(Long id, Long handlerId, String repairDescription,
                               String partsUsed, Double repairCost) {
        RepairRequest repair = repairRequestMapper.selectById(id);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }

        if (!RepairStatus.PROCESSING.name().equals(repair.getStatus())) {
            throw new RuntimeException("报修记录未在处理中");
        }

        // 验证是否是任务被分配的人或管理员/老师
        if (repair.getHandlerId() != null && !repair.getHandlerId().equals(handlerId)) {
            throw new RuntimeException("只能完成分配给自己的任务");
        }

        repair.setStatus(RepairStatus.COMPLETED.name());
        repair.setRepairDescription(repairDescription);
        repair.setPartsUsed(partsUsed);
        repair.setRepairCost(java.math.BigDecimal.valueOf(repairCost));
        repair.setCompleteTime(LocalDateTime.now());
        repairRequestMapper.updateById(repair);

        // 更新设备状态为正常
        Equipment equipment = equipmentMapper.selectById(repair.getEquipmentId());
        if (equipment != null) {
            equipment.setStatus("NORMAL");
            equipmentMapper.updateById(equipment);
        }

        // 发送通知给报修人（省略）
    }

    /**
     * 驳回报修
     */
    @Transactional(rollbackFor = Exception.class)
    public void rejectRepair(Long id, String remark) {
        RepairRequest repair = repairRequestMapper.selectById(id);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }

        repair.setStatus(RepairStatus.REJECTED.name());
        repair.setRemark(remark);
        repairRequestMapper.updateById(repair);

        // 更新设备状态为正常
        Equipment equipment = equipmentMapper.selectById(repair.getEquipmentId());
        if (equipment != null) {
            equipment.setStatus("NORMAL");
            equipmentMapper.updateById(equipment);
        }
    }

    /**
     * 获取待处理报修数量
     */
    public Long getPendingRepairCount() {
        return repairRequestMapper.selectCount(
                new LambdaQueryWrapper<RepairRequest>()
                        .eq(RepairRequest::getStatus, RepairStatus.PENDING.name())
        );
    }

    /**
     * 生成报修编号
     */
    private String generateRepairNo() {
        return "REP"+System.currentTimeMillis();
}

    /**
     * 获取所有老师用户（用于任务分配）
     */
    public List<SysUser> getAllTeachers() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getRole, "TEACHER");
        wrapper.eq(SysUser::getStatus, 1);  // 只返回启用状态的用户
        wrapper.orderByAsc(SysUser::getRealName);
        return userMapper.selectList(wrapper);
    }

    /**
     * 获取报修任务统计
     */
    public TaskStatsVO getRepairTaskStats() {
        TaskStatsVO stats = new TaskStatsVO();
        
        // 统计总数
        Long total = repairRequestMapper.selectCount(null);
        stats.setTotalRepairTasks(total);
        
        // 统计待处理
        Long pending = repairRequestMapper.selectCount(
                new LambdaQueryWrapper<RepairRequest>()
                        .eq(RepairRequest::getStatus, RepairStatus.PENDING.name())
        );
        stats.setPendingRepairTasks(pending);
        
        // 统计处理中
        Long processing = repairRequestMapper.selectCount(
                new LambdaQueryWrapper<RepairRequest>()
                        .eq(RepairRequest::getStatus, RepairStatus.PROCESSING.name())
        );
        stats.setProcessingRepairTasks(processing);
        
        // 统计各老师的任务数
        List<SysUser> teachers = getAllTeachers();
        List<TaskStatsVO.TeacherTaskCount> taskCounts = new java.util.ArrayList<>();
        
        for (SysUser teacher : teachers) {
            Long count = repairRequestMapper.selectCount(
                    new LambdaQueryWrapper<RepairRequest>()
                            .eq(RepairRequest::getHandlerId, teacher.getId())
                            .in(RepairRequest::getStatus, RepairStatus.PENDING.name(), RepairStatus.PROCESSING.name())
            );
            taskCounts.add(new TaskStatsVO.TeacherTaskCount(
                    teacher.getId(),
                    teacher.getUsername(),
                    teacher.getRealName(),
                    count
            ));
        }
        stats.setRepairTaskCounts(taskCounts);
        
        return stats;
    }
}
