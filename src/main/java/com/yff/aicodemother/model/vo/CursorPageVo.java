package com.yff.aicodemother.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 游标分页响应对象
 *
 * @author yff
 * @date 2026-02-08 15:34:13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorPageVo<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 是否还有下一页
     */
    private Boolean hasMore;

    /**
     * 下一次查询要用的游标信息
     */
    private CursorInfo nextCursor;

}
