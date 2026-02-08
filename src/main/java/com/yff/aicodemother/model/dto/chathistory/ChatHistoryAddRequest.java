package com.yff.aicodemother.model.dto.chathistory;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 保存对话消息请求
 *
 * @author yff
 * @since 2026-02-08
 */
@Data
public class ChatHistoryAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 消息类型（user/ai/error）
     */
    private String messageType;

    /**
     * 应用 ID
     */
    private Long appId;

    /**
     * 用户 ID
     */
    private Long userId;

}
