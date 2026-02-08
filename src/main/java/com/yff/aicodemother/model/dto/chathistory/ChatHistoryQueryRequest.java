package com.yff.aicodemother.model.dto.chathistory;

import com.yff.aicodemother.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户查询应用对话历史请求
 *
 * @author yff
 * @since 2026-02-08
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用 ID（必填）
     */
    private Long appId;

    /**
     * 限制每页最大数量为 10（默认加载最新 10 条）
     */
    @Override
    public int getPageSize() {
        int size = super.getPageSize();
        return Math.min(size, 10);
    }

}
