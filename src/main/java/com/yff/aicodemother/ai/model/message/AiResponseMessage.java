package com.yff.aicodemother.ai.model.message;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 *
 * AI响应的消息
 * @author yff
 * @date 2026-02-11 16:25:39
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AiResponseMessage extends StreamMessage{

    private String data;

    public AiResponseMessage(String data) {
        super(StreamMessageTypeEnum.AI_RESPONSER.getValue());
        this.data = data;
    }




}
