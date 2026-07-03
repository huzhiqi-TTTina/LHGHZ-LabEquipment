package com.lab.equipment.controller;

import com.lab.equipment.common.PageResult;
import com.lab.equipment.common.Result;
import com.lab.equipment.dto.EquipmentDTO;
import com.lab.equipment.dto.EquipmentVO;
import com.lab.equipment.dto.PageQueryDTO;
import com.lab.equipment.entity.Equipment;
import com.lab.equipment.security.CustomeUserDetails;
import com.lab.equipment.service.AuthService;
import com.lab.equipment.service.EquipmentService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 设备控制器
 */
@RestController
@RequestMapping("/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final AuthService authService;

    /**
     * 分页查询设备列表
     */
    @GetMapping("/list")
    public Result<PageResult<EquipmentVO>> list(PageQueryDTO query) {
        PageResult<EquipmentVO> result = equipmentService.getEquipmentList(query);
        return Result.success(result);
    }

    /**
     * 根据ID获取设备详情
     */
    @GetMapping("/{id}")
    public Result<EquipmentVO> getById(@PathVariable Long id) {
        EquipmentVO equipment = equipmentService.getEquipmentById(id);
        return Result.success(equipment);
    }

    /**
     * 新增设备（管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> add(@Valid @RequestBody EquipmentDTO dto) {
        Long id = equipmentService.addEquipment(dto);
        return Result.success("设备添加成功", id);
    }

    /**
     * 更新设备（管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody EquipmentDTO dto) {
        equipmentService.updateEquipment(id, dto);
        return Result.success("设备更新成功", null);
    }

    /**
     * 删除设备（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return Result.success("设备删除成功", null);
    }

    /**
     * 设备出库登记（管理员）
     */
    @PostMapping("/{id}/borrow")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> borrow(@PathVariable Long id, @RequestParam Long userId,
                                @RequestParam(required = false) String purpose) {
        equipmentService.borrowEquipment(id, userId, purpose);
        return Result.success("设备出库成功", null);
    }

    /**
     * 设备归还确认（管理员）
     */
    @PostMapping("/{id}/return")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> returnEquipment(@PathVariable Long id,
                                         @RequestParam(defaultValue = "GOOD") String condition) {
        equipmentService.returnEquipment(id, condition);
        return Result.success("设备归还成功", null);
    }

    /**
     * 导出设备列表（管理员）
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void export(HttpServletResponse response) throws IOException {
        equipmentService.exportEquipmentList(response);
    }

    /**
     * 获取二维码（通过设备ID）
     */
    @GetMapping("/{id}/qrcode")
    public Result<EquipmentVO> getQrCode(@PathVariable Long id) {
        EquipmentVO equipment = equipmentService.getEquipmentById(id);
        return Result.success(equipment);
    }

    /**
     * 批量重新生成所有设备的二维码（管理员）
     */
    @PostMapping("/qrcode/regenerate-all")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Integer> regenerateAllQrCodes() {
        int count = equipmentService.regenerateAllQrCodes();
        return Result.success("成功重新生成 " + count + " 个设备的二维码", count);
    }

    /**
     * 重新生成指定设备的二维码（管理员）
     */
    @PostMapping("/{id}/qrcode/regenerate")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> regenerateQrCode(@PathVariable Long id) {
        equipmentService.regenerateQrCode(id);
        return Result.success("二维码重新生成成功", null);
    }
}
