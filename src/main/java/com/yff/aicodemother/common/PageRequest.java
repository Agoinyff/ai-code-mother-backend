package com.yff.aicodemother.common;


import lombok.Data;

/**
 * @author yff
 * @date 2026-02-02 10:13:05
 * * 分页请求参数
 */
@Data
public class PageRequest {

    /**
     * 当前页码
     */
    private int pageNum = 1;

    /**
     * 每页大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方式  ascend 或 descend   默认 descend
     */
    private String sortOrder = "descend";

}
