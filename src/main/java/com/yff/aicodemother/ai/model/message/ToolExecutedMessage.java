package com.yff.aicodemother.ai.model.message;


import dev.langchain4j.service.TokenStream;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * 工具执行结果消息
 *
 *
 * @author yff
 * @date 2026-02-11 16:38:51
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ToolExecutedMessage extends StreamMessage{

    private String id;
    private String name;
    private String arguments;
    private String result;

    public ToolExecutedMessage(String id, String name, String arguments, String result) {
        super(StreamMessageTypeEnum.TOOL_EXECUTED.getValue());
        this.id = id;
        this.name = name;
        this.arguments = arguments;
        this.result = result;
    }






}
