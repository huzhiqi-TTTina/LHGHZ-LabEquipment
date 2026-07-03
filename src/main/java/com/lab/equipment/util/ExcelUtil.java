package com.lab.equipment.util;

import com.lab.equipment.common.PageResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Excel工具类
 */
@Slf4j
@Component
public class ExcelUtil {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 导出Excel
     */
    public <T> void exportExcel(HttpServletResponse response, List<T> dataList, String fileName, String sheetName) throws IOException {
        // 创建工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);

        if (dataList == null || dataList.isEmpty()) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8));
            workbook.write(response.getOutputStream());
            workbook.close();
            return;
        }

        // 获取字段
        Field[] fields = dataList.get(0).getClass().getDeclaredFields();

        // 创建表头样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 创建表头行
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < fields.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(fields[i].getName());
            cell.setCellStyle(headerStyle);
        }

        // 创建数据行
        int rowNum = 1;
        for (T data : dataList) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                Cell cell = row.createCell(i);
                try {
                    Object value = field.get(data);
                    setCellValue(cell, value);
                } catch (IllegalAccessException e) {
                    log.error("获取字段值失败: {}", e.getMessage());
                }
            }
        }

        // 自动调整列宽
        for (int i = 0; i < fields.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8));

        // 写入输出流
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    /**
     * 设置单元格值
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue(DATE_FORMAT.format((Date) value));
        } else {
            cell.setCellValue(value.toString());
        }
    }
}
