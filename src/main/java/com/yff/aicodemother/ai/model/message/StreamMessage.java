package com.yff.aicodemother.ai.model.message;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * 流式响应消息基类
 *
 * @author yff
 * @date 2026-02-11 16:24:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamMessage {

    private String type;


}
