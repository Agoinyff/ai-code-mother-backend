package com.yff.aicodemother.model.dto.chathistory;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 游标查询请求类
 *
 * @author yff
 * @date 2026-02-08 15:01:20
 */
@Data
public class ChatHistoryCursorQueryRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用 ID（必填）
     */
    private Long appId;

    /**
     * 每页查询条数（默认10条）
     */
    private Integer pageSize = 10;

    /**
     * 最后一条记录的创建时间
     * 用于游标分页，获取早于此时间的记录
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastTime;

    /**
     * 最后一条记录的ID（雪花算法生成）
     * 用于游标分页，当时间相同时通过ID进一步排序
     */
    private Long lastId;

}
