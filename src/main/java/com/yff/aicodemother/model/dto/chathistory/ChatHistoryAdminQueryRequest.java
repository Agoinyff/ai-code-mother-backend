package com.yff.aicodemother.model.dto.chathistory;

import com.yff.aicodemother.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员查询所有对话历史请求
 *
 * @author yff
 * @since 2026-02-08
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryAdminQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 对话历史 ID
     */
    private Long id;

    /**
     * 应用 ID
     */
    private Long appId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 消息类型（user/ai/error）
     */
    private String messageType;

    /**
     * 消息内容（模糊查询）
     */
    private String message;

}
