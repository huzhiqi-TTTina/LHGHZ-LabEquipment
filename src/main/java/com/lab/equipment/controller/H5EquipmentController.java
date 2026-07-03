package com.lab.equipment.controller;

import com.lab.equipment.common.Result;
import com.lab.equipment.dto.H5EquipmentVO;
import com.lab.equipment.entity.Equipment;
import com.lab.equipment.entity.EquipmentCategory;
import com.lab.equipment.entity.Laboratory;
import com.lab.equipment.enums.EquipmentStatus;
import com.lab.equipment.mapper.EquipmentCategoryMapper;
import com.lab.equipment.mapper.EquipmentMapper;
import com.lab.equipment.mapper.LaboratoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * H5设备控制器（扫码后的操作接口，无需认证）
 */
@RestController
@RequestMapping("/h5/equipment")
@RequiredArgsConstructor
public class H5EquipmentController {

    private final EquipmentMapper equipmentMapper;
    private final EquipmentCategoryMapper categoryMapper;
    private final LaboratoryMapper laboratoryMapper;

    /**
     * 根据设备ID获取设备信息（H5专用）
     */
    @GetMapping("/{id}")
    public Result<?> getEquipmentInfo(@PathVariable Long id) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            return Result.error(404, "设备不存在");
        }

        H5EquipmentVO vo = convertToH5VO(equipment);
        return Result.success(vo);
    }

    /**
     * 设备出库（H5专用，无需认证）
     */
    @PostMapping("/{id}/checkout")
    public Result<?> checkout(@PathVariable Long id,
                              @RequestParam(required = false) String operator) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            return Result.error(404, "设备不存在");
        }

        if (!EquipmentStatus.NORMAL.name().equals(equipment.getStatus())) {
            return Result.error(400, "设备当前状态不可出库，当前状态：" + getStatusText(equipment.getStatus()));
        }

        // 更新设备状态为借出
        equipment.setStatus(EquipmentStatus.BORROWED.name());
        equipmentMapper.updateById(equipment);

        return Result.success("设备出库成功", convertToH5VO(equipment));
    }

    /**
     * 设备入库（H5专用，无需认证）
     */
    @PostMapping("/{id}/checkin")
    public Result<?> checkin(@PathVariable Long id,
                             @RequestParam(required = false) String operator,
                             @RequestParam(defaultValue = "GOOD") String condition) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            return Result.error(404, "设备不存在");
        }

        if (!EquipmentStatus.BORROWED.name().equals(equipment.getStatus())) {
            return Result.error(400, "设备当前状态为" + getStatusText(equipment.getStatus()) + "，无需入库");
        }

        // 更新设备状态为正常，并设置完好程度
        equipment.setStatus(EquipmentStatus.NORMAL.name());
        equipment.setEquipmentCondition(condition);
        equipmentMapper.updateById(equipment);

        return Result.success("设备入库成功", convertToH5VO(equipment));
    }

    /**
     * 转换为H5 VO
     */
    private H5EquipmentVO convertToH5VO(Equipment equipment) {
        H5EquipmentVO vo = new H5EquipmentVO();
        BeanUtils.copyProperties(equipment, vo);

        // 设置状态文本
        vo.setStatusText(getStatusText(equipment.getStatus()));
        vo.setConditionText(getConditionText(equipment.getEquipmentCondition()));

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
                return "在库";
            case "BORROWED":
                return "借出中";
            case "MAINTENANCE":
                return "维修中";
            case "SCRAPPED":
                return "已报废";
            default:
                return status;
        }
    }

    /**
     * 获取完好程度文本
     */
    private String getConditionText(String condition) {
        if (condition == null) return "";
        switch (condition) {
            case "GOOD":
                return "完好";
            case "DAMAGED":
                return "损坏";
            case "SERIOUS":
                return "严重损坏";
            default:
                return condition;
        }
    }
}
