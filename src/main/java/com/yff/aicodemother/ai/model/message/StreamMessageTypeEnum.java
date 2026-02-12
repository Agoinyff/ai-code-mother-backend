package com.yff.aicodemother.ai.model.message;

import lombok.Getter;

/**
 * @author yff
 * @date 2026-02-11 16:28:20
 */
@Getter
public enum StreamMessageTypeEnum {

    AI_RESPONSER("ai_response","AI响应"),
    TOOL_REQUEST("tool_request","工具请求"),
    TOOL_EXECUTED("tool_executed","工具执行结果");



    private final String value;
    private final String text;

    StreamMessageTypeEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }


    //根据值获取枚举
    public static StreamMessageTypeEnum getEnumByValue(String value) {

        if (value == null || value.isEmpty()) {
            return null;
        }

        for (StreamMessageTypeEnum anEnum : StreamMessageTypeEnum.values()) {
            if (anEnum.getValue().equals(value)) {
                return anEnum;
            }
        }
        return null;
    }




}
