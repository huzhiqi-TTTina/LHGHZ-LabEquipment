package com.lab.equipment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.equipment.common.PageResult;
import com.lab.equipment.dto.EquipmentDTO;
import com.lab.equipment.dto.EquipmentVO;
import com.lab.equipment.dto.PageQueryDTO;
import com.lab.equipment.entity.Equipment;
import com.lab.equipment.entity.EquipmentCategory;
import com.lab.equipment.entity.Laboratory;
import com.lab.equipment.entity.SysUser;
import com.lab.equipment.enums.EquipmentStatus;
import com.lab.equipment.mapper.EquipmentCategoryMapper;
import com.lab.equipment.mapper.EquipmentMapper;
import com.lab.equipment.mapper.LaboratoryMapper;
import com.lab.equipment.mapper.SysUserMapper;
import com.lab.equipment.util.QrCodeUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 设备服务
 */
@Service
@RequiredArgsConstructor
public class EquipmentService extends ServiceImpl<EquipmentMapper, Equipment> {


    private final EquipmentMapper equipmentMapper;
    private final EquipmentCategoryMapper categoryMapper;
    private final LaboratoryMapper laboratoryMapper;
    private final SysUserMapper userMapper;
    private final QrCodeUtil qrCodeUtil;

    /**
     * 分页查询设备列表
     */
    public PageResult<EquipmentVO> getEquipmentList(PageQueryDTO query) {
        // 构建查询条件
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Equipment::getEquipmentName, query.getKeyword())
                    .or().like(Equipment::getEquipmentNo, query.getKeyword())
                    .or().like(Equipment::getBrand, query.getKeyword())
                    .or().like(Equipment::getModel, query.getKeyword()));
        }

        // 排序
        if (StringUtils.hasText(query.getSortField())) {
            wrapper.orderBy(true, "asc".equals(query.getSortOrder()),
                    query.getSortField().equals("equipmentNo") ? Equipment::getEquipmentNo :
                    query.getSortField().equals("equipmentName") ? Equipment::getEquipmentName :
                    Equipment::getCreateTime);
        } else {
            wrapper.orderByDesc(Equipment::getCreateTime);
        }

        // 分页查询
        Page<Equipment> page = equipmentMapper.selectPage(
                new Page<>(query.getCurrent(), query.getSize()),
                wrapper
        );

        // 转换为VO
        List<EquipmentVO> voList = new ArrayList<>();
        for (Equipment equipment : page.getRecords()) {
            voList.add(convertToVO(equipment));
        }

        return PageResult.of(page.getTotal(), voList, page.getCurrent(), page.getSize());
    }

    /**
     * 根据ID获取设备详情
     */
    public EquipmentVO getEquipmentById(Long id) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }
        return convertToVO(equipment);
    }

    /**
     * 新增设备
     */
    @Transactional(rollbackFor = Exception.class)
    public Long addEquipment(EquipmentDTO dto) {
        // 检查设备编号是否已存在
        Equipment existEquipment = equipmentMapper.selectOne(
                new LambdaQueryWrapper<Equipment>()
                        .eq(Equipment::getEquipmentNo, dto.getEquipmentNo())
        );
        if (existEquipment != null) {
            throw new RuntimeException("设备编号已存在");
        }

        // 创建设备
        Equipment equipment = new Equipment();
        BeanUtils.copyProperties(dto, equipment);
        equipment.setStatus(EquipmentStatus.NORMAL.name());
        equipment.setEquipmentCondition("GOOD");

        equipmentMapper.insert(equipment);

        // 生成唯一标识码和二维码
        try {
            String uniqueCode = qrCodeUtil.generateUniqueCode(equipment.getId());
            String qrCodeContent = qrCodeUtil.generateEquipmentQrCodeUrl(equipment.getId());
            String fileName = "equipment_" + equipment.getId() + "_" + System.currentTimeMillis() + ".png";
            String qrCodeImageUrl = qrCodeUtil.generateQrCode(qrCodeContent, fileName);

            equipment.setUniqueCode(uniqueCode);
            equipment.setQrCode(qrCodeContent);
            equipment.setQrCodeImage(qrCodeImageUrl);
            equipmentMapper.updateById(equipment);
        } catch (Exception e) {
            throw new RuntimeException("生成二维码失败: " + e.getMessage());
        }

        return equipment.getId();
    }

    /**
     * 更新设备
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateEquipment(Long id, EquipmentDTO dto) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }

        // 检查设备编号是否被其他设备占用
        Equipment existEquipment = equipmentMapper.selectOne(
                new LambdaQueryWrapper<Equipment>()
                        .eq(Equipment::getEquipmentNo, dto.getEquipmentNo())
                        .ne(Equipment::getId, id)
        );
        if (existEquipment != null) {
            throw new RuntimeException("设备编号已被其他设备使用");
        }

        // 更新设备信息
        BeanUtils.copyProperties(dto, equipment);
        equipmentMapper.updateById(equipment);
    }

    /**
     * 删除设备
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteEquipment(Long id) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }

        // 检查设备状态，不能删除借出或维修中的设备
        if (EquipmentStatus.BORROWED.name().equals(equipment.getStatus())
                || EquipmentStatus.MAINTENANCE.name().equals(equipment.getStatus())) {
            throw new RuntimeException("设备当前状态不允许删除");
        }

        equipmentMapper.deleteById(id);
    }

    /**
     * 设备出库登记
     */
    @Transactional(rollbackFor = Exception.class)
    public void borrowEquipment(Long equipmentId, Long userId, String purpose) {
        Equipment equipment = equipmentMapper.selectById(equipmentId);
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }

        if (!EquipmentStatus.NORMAL.name().equals(equipment.getStatus())) {
            throw new RuntimeException("设备当前状态不可借出");
        }

        // 更新设备状态
        equipment.setStatus(EquipmentStatus.BORROWED.name());
        equipmentMapper.updateById(equipment);
    }

    /**
     * 设备归还确认
     */
    @Transactional(rollbackFor = Exception.class)
    public void returnEquipment(Long equipmentId, String condition) {
        Equipment equipment = equipmentMapper.selectById(equipmentId);
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }

        // 更新设备状态和完好程度
        equipment.setStatus(EquipmentStatus.NORMAL.name());
        equipment.setEquipmentCondition(condition);
        equipmentMapper.updateById(equipment);
    }

    /**
     * 导出设备列表
     */
    public void exportEquipmentList(HttpServletResponse response) throws IOException {
        List<Equipment> equipmentList = equipmentMapper.selectList(null);
        // 这里需要实现Excel导出逻辑
        // 简化版：实际应使用ExcelUtil
    }

    /**
     * 批量重新生成所有设备的二维码
     */
    @Transactional(rollbackFor = Exception.class)
    public int regenerateAllQrCodes() {
        List<Equipment> equipmentList = equipmentMapper.selectList(null);
        int successCount = 0;
        int failCount = 0;

        for (Equipment equipment : equipmentList) {
            try {
                // 生成唯一标识码和二维码
                String uniqueCode = qrCodeUtil.generateUniqueCode(equipment.getId());
                String qrCodeContent = qrCodeUtil.generateEquipmentQrCodeUrl(equipment.getId());
                String fileName = "equipment_" + equipment.getId() + "_" + System.currentTimeMillis() + ".png";
                String qrCodeImageUrl = qrCodeUtil.generateQrCode(qrCodeContent, fileName);

                equipment.setUniqueCode(uniqueCode);
                equipment.setQrCode(qrCodeContent);
                equipment.setQrCodeImage(qrCodeImageUrl);
                equipmentMapper.updateById(equipment);

                successCount++;
            } catch (Exception e) {
                failCount++;
                // 记录错误日志但不中断整个流程
                System.err.println("设备ID " + equipment.getId() + " 二维码生成失败: " + e.getMessage());
            }
        }

        return successCount;
    }

    /**
     * 重新生成指定设备的二维码
     */
    @Transactional(rollbackFor = Exception.class)
    public void regenerateQrCode(Long id) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }

        try {
            // 生成唯一标识码和二维码
            String uniqueCode = qrCodeUtil.generateUniqueCode(equipment.getId());
            String qrCodeContent = qrCodeUtil.generateEquipmentQrCodeUrl(equipment.getId());
            String fileName = "equipment_" + equipment.getId() + "_" + System.currentTimeMillis() + ".png";
            String qrCodeImageUrl = qrCodeUtil.generateQrCode(qrCodeContent, fileName);

            equipment.setUniqueCode(uniqueCode);
            equipment.setQrCode(qrCodeContent);
            equipment.setQrCodeImage(qrCodeImageUrl);
            equipmentMapper.updateById(equipment);
        } catch (Exception e) {
            throw new RuntimeException("生成二维码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 转换为VO
     */
    private EquipmentVO convertToVO(Equipment equipment) {
        EquipmentVO vo = new EquipmentVO();
        BeanUtils.copyProperties(equipment, vo);

        // 设置状态文本
        vo.setStatusText(getStatusText(equipment.getStatus()));

        // 兼容处理：如果uniqueCode为空，生成一个显示用的
        if (vo.getUniqueCode() == null || vo.getUniqueCode().isEmpty()) {
            vo.setUniqueCode(qrCodeUtil.generateUniqueCode(equipment.getId()));
        }

        // 获取分类名称
        if (equipment.getCategoryId() != null) {
            EquipmentCategory category = categoryMapper.selectById(equipment.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }

        // 获取实验室名称
        if (equipment.getLaboratoryId() != null) {
            Laboratory laboratory = laboratoryMapper.selectById(equipment.getLaboratoryId());
            if (laboratory != null) {
                vo.setLaboratoryName(laboratory.getLabName());
            }
        }

        return vo;
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(String status) {
        if (status == null) return "";
        switch (status) {
            case "NORMAL":
                return "正常";
            case "BORROWED":
                return "借出中";
            case "MAINTENANCE":
                return "维修中";
            case "SCRAPPED":
                return "报废";
            default:
                return status;
        }
    }
}
