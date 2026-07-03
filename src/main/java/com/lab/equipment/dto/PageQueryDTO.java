package com.lab.equipment.dto;

import lombok.Data;

/**
 * 分页查询DTO
 */
@Data
public class PageQueryDTO {

    /**
     * 当前页
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方式：asc/desc
     */
    private String sortOrder = "desc";

    /**
     * 关键字
     */
    private String keyword;
}
