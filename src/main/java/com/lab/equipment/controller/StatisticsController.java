package com.lab.equipment.controller;

import com.lab.equipment.common.Result;
import com.lab.equipment.dto.StatisticsDTO;
import com.lab.equipment.service.StatisticsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 统计报表控制器
 */
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取统计数据
     */
    @GetMapping
    public Result<StatisticsDTO> getStatistics() {
        StatisticsDTO statistics = statisticsService.getStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取设备利用率趋势
     */
    @GetMapping("/utilization-trend")
    public Result<Object> getUtilizationTrend(@RequestParam(defaultValue = "7") Integer days) {
        Object trend = statisticsService.getUtilizationTrend(days);
        return Result.success(trend);
    }

    /**
     * 获取设备按分类统计
     */
    @GetMapping("/by-category")
    public Result<Object> getEquipmentByCategory() {
        Object data = statisticsService.getEquipmentByCategory();
        return Result.success(data);
    }

    /**
     * 导出统计报表
     */
    @GetMapping("/export")
    public void exportStatistics(HttpServletResponse response) throws IOException {
        statisticsService.exportStatistics(response);
    }
}
