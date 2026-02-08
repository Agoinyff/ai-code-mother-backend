package com.yff.aicodemother.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 游标信息对象
 * 用于游标分页查询，包含最后一条记录的时间和ID
 *
 * @author yff
 * @date 2026-02-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 最后一条记录的创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastTime;

    /**
     * 最后一条记录的ID（雪花算法生成）
     */
    private Long lastId;

}
