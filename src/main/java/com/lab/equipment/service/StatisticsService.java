package com.lab.equipment.service;

import com.lab.equipment.dto.StatisticsDTO;
import com.lab.equipment.entity.BorrowRecord;
import com.lab.equipment.entity.Equipment;
import com.lab.equipment.entity.EquipmentReservation;
import com.lab.equipment.entity.RepairRequest;
import com.lab.equipment.enums.EquipmentStatus;
import com.lab.equipment.enums.RepairStatus;
import com.lab.equipment.enums.ReservationStatus;
import com.lab.equipment.mapper.BorrowRecordMapper;
import com.lab.equipment.mapper.EquipmentMapper;
import com.lab.equipment.mapper.EquipmentReservationMapper;
import com.lab.equipment.mapper.RepairRequestMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * 统计服务
 */
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final EquipmentMapper equipmentMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final EquipmentReservationMapper reservationMapper;
    private final RepairRequestMapper repairRequestMapper;

    /**
     * 获取统计数据
     */
    public StatisticsDTO getStatistics() {
        StatisticsDTO dto = new StatisticsDTO();

        // 设备统计
        dto.setTotalEquipment(equipmentMapper.selectCount(null));
        dto.setNormalEquipment(equipmentMapper.countByStatus(EquipmentStatus.NORMAL.name()));
        dto.setBorrowedEquipment(equipmentMapper.countByStatus(EquipmentStatus.BORROWED.name()));
        dto.setMaintenanceEquipment(equipmentMapper.countByStatus(EquipmentStatus.MAINTENANCE.name()));
        dto.setScrappedEquipment(equipmentMapper.countByStatus(EquipmentStatus.SCRAPPED.name()));

        // 借用统计
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.now().with(LocalTime.MAX);
        LocalDateTime monthStart = todayStart.withDayOfMonth(1).with(LocalTime.MIN);

        dto.setTodayBorrowCount(borrowRecordMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BorrowRecord>()
                        .between(BorrowRecord::getBorrowDate, todayStart, todayEnd)
        ));

        dto.setMonthBorrowCount(borrowRecordMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BorrowRecord>()
                        .between(BorrowRecord::getBorrowDate, monthStart, todayEnd)
        ));

        // 预约统计
        dto.setPendingApprovalCount(reservationMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EquipmentReservation>()
                        .eq(EquipmentReservation::getStatus, ReservationStatus.PENDING.name())
        ));

        // 报修统计
        dto.setPendingRepairCount(repairRequestMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RepairRequest>()
                        .eq(RepairRequest::getStatus, RepairStatus.PENDING.name())
        ));

        // 计算利用率
        Long availableCount = equipmentMapper.countByStatus(EquipmentStatus.NORMAL.name())
                + equipmentMapper.countByStatus(EquipmentStatus.BORROWED.name());
        if (dto.getTotalEquipment() > 0) {
            dto.setUtilizationRate((double) dto.getBorrowedEquipment() / dto.getTotalEquipment() * 100);
        }

        // 计算损坏率（这里简化为维修中设备比例）
        if (dto.getTotalEquipment() > 0) {
            dto.setDamageRate((double) dto.getMaintenanceEquipment() / dto.getTotalEquipment() * 100);
        }

        return dto;
    }

    /**
     * 获取设备利用率趋势数据
     */
    public Object getUtilizationTrend(Integer days) {
        // 返回最近N天的利用率数据
        // 这里简化实现，实际需要编写更复杂的SQL
        return null;
    }

    /**
     * 获取设备按分类统计
     */
    public Object getEquipmentByCategory() {
        // 返回设备按分类统计的数据
        // 这里简化实现
        return null;
    }

    /**
     * 导出统计数据到Excel
     */
    public void exportStatistics(HttpServletResponse response) throws IOException {
        StatisticsDTO dto = getStatistics();

        // 创建工作簿
        Workbook workbook = new XSSFWorkbook();

        // 创建标题样式
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        // 创建表头样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // 创建数据样式
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // ========== 第一个sheet：设备概况 ==========
        Sheet sheet1 = workbook.createSheet("设备概况");

        // 标题
        Row titleRow1 = sheet1.createRow(0);
        Cell titleCell1 = titleRow1.createCell(0);
        titleCell1.setCellValue("实验室设备统计报表");
        titleCell1.setCellStyle(titleStyle);
        sheet1.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 3));

        // 报告时间
        Row dateRow = sheet1.createRow(1);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("报告时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        dateCell.setCellStyle(dataStyle);
        sheet1.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 3));

        // 设备统计表格
        String[][] equipmentData = {
                {"统计项目", "数量", "统计项目", "数量"},
                {"设备总数", String.valueOf(dto.getTotalEquipment()), "正常设备", String.valueOf(dto.getNormalEquipment())},
                {"借出设备", String.valueOf(dto.getBorrowedEquipment()), "维修中设备", String.valueOf(dto.getMaintenanceEquipment())},
                {"报废设备", String.valueOf(dto.getScrappedEquipment()), "设备利用率", String.format("%.2f%%", dto.getUtilizationRate())},
                {"设备损坏率", String.format("%.2f%%", dto.getDamageRate()), "", ""}
        };

        int rowNum = 3;
        for (String[] rowData : equipmentData) {
            Row row = sheet1.createRow(rowNum++);
            for (int i = 0; i < rowData.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(rowData[i]);
                if (rowNum == 4) {
                    cell.setCellStyle(headerStyle);
                } else {
                    cell.setCellStyle(dataStyle);
                }
            }
        }

        // 自动调整列宽
        for (int i = 0; i < 4; i++) {
            sheet1.autoSizeColumn(i);
        }

        // ========== 第二个sheet：借用统计 ==========
        Sheet sheet2 = workbook.createSheet("借用统计");

        // 标题
        Row titleRow2 = sheet2.createRow(0);
        Cell titleCell2 = titleRow2.createCell(0);
        titleCell2.setCellValue("借用统计");
        titleCell2.setCellStyle(titleStyle);
        sheet2.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));

        String[][] borrowData = {
                {"统计项目", "数量"},
                {"今日借用次数", String.valueOf(dto.getTodayBorrowCount())},
                {"本月借用次数", String.valueOf(dto.getMonthBorrowCount())}
        };

        rowNum = 2;
        for (String[] rowData : borrowData) {
            Row row = sheet2.createRow(rowNum++);
            for (int i = 0; i < rowData.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(rowData[i]);
                if (rowNum == 3) {
                    cell.setCellStyle(headerStyle);
                } else {
                    cell.setCellStyle(dataStyle);
                }
            }
        }

        for (int i = 0; i < 2; i++) {
            sheet2.autoSizeColumn(i);
        }

        // ========== 第三个sheet：待处理事项 ==========
        Sheet sheet3 = workbook.createSheet("待处理事项");

        // 标题
        Row titleRow3 = sheet3.createRow(0);
        Cell titleCell3 = titleRow3.createCell(0);
        titleCell3.setCellValue("待处理事项");
        titleCell3.setCellStyle(titleStyle);
        sheet3.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));

        String[][] pendingData = {
                {"事项类型", "数量"},
                {"待审核预约", String.valueOf(dto.getPendingApprovalCount())},
                {"待处理报修", String.valueOf(dto.getPendingRepairCount())}
        };

        rowNum = 2;
        for (String[] rowData : pendingData) {
            Row row = sheet3.createRow(rowNum++);
            for (int i = 0; i < rowData.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(rowData[i]);
                if (rowNum == 3) {
                    cell.setCellStyle(headerStyle);
                } else {
                    cell.setCellStyle(dataStyle);
                }
            }
        }

        for (int i = 0; i < 2; i++) {
            sheet3.autoSizeColumn(i);
        }

        // 设置响应头
        String fileName = "统计报表_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8));

        // 写入输出流
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
