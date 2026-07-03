package com.lab.equipment.controller;

import com.lab.equipment.common.PageResult;
import com.lab.equipment.common.Result;
import com.lab.equipment.dto.PageQueryDTO;
import com.lab.equipment.dto.ReservationDTO;
import com.lab.equipment.entity.EquipmentReservation;
import com.lab.equipment.security.CustomeUserDetails;
import com.lab.equipment.service.AuthService;
import com.lab.equipment.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 设备预约控制器
 */
@RestController
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final AuthService authService;

    /**
     * 分页查询预约列表
     */
    @GetMapping("/list")
    public Result<PageResult<EquipmentReservation>> list(PageQueryDTO query) {
        PageResult<EquipmentReservation> result = reservationService.getReservationList(query);
        return Result.success(result);
    }

    /**
     * 获取我的预约列表
     */
    @GetMapping("/my")
    public Result<PageResult<EquipmentReservation>> myReservations(
            PageQueryDTO query,
            @AuthenticationPrincipal CustomeUserDetails userDetails) {
        PageResult<EquipmentReservation> result = reservationService.getMyReservationList(
                userDetails.getUserId(), query);
        return Result.success(result);
    }

    /**
     * 根据ID获取预约详情
     */
    @GetMapping("/{id}")
    public Result<EquipmentReservation> getById(@PathVariable Long id) {
        EquipmentReservation reservation = reservationService.getReservationById(id);
        return Result.success(reservation);
    }

    /**
     * 创建预约
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ReservationDTO dto,
                               @AuthenticationPrincipal CustomeUserDetails userDetails) {
        Long id = reservationService.createReservation(userDetails.getUserId(), dto);
        return Result.success("预约创建成功", id);
    }

    /**
     * 审核预约（管理员）
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> approve(@PathVariable Long id,
                                 @RequestParam String status,
                                 @RequestParam(required = false) String remark,
                                 @AuthenticationPrincipal CustomeUserDetails userDetails) {
        reservationService.approveReservation(id, status, remark, userDetails.getUserId());
        return Result.success("预约审核成功", null);
    }

    /**
     * 取消预约
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                               @AuthenticationPrincipal CustomeUserDetails userDetails) {
        reservationService.cancelReservation(id, userDetails.getUserId());
        return Result.success("预约取消成功", null);
    }
}
