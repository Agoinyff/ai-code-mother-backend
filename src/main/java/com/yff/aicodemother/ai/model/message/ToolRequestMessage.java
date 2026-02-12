package com.yff.aicodemother.ai.model.message;


import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.model.chat.response.PartialToolCall;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 *
 * 工具的调用消息
 *
 *
 * @author yff
 * @date 2026-02-11 16:33:02
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ToolRequestMessage extends StreamMessage{

    private String id;
    private String name;
    private String arguments;

    public ToolRequestMessage(PartialToolCall partialToolCall) {
        super(StreamMessageTypeEnum.TOOL_REQUEST.getValue());
        this.id = partialToolCall.id();
        this.name = partialToolCall.name();
        this.arguments = partialToolCall.partialArguments();
    }


}
