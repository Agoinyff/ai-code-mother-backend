package com.yff.aicodemother.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 消息类型枚举
 *
 * @author yff
 * @date 2026-02-08
 */
@Getter
public enum MessageTypeEnum {

    USER("用户消息", "user"),
    AI("AI消息", "ai"),
    ERROR("错误消息", "error");
    //TODO 把ERROR合并到AI消息里

    private final String text;
    private final String value;

    MessageTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 通过 value 值找到对应的枚举
     *
     * @param value 枚举值
     * @return 对应的枚举对象
     */
    public static MessageTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (MessageTypeEnum messageTypeEnum : MessageTypeEnum.values()) {
            if (messageTypeEnum.value.equals(value)) {
                return messageTypeEnum;
            }
        }
        return null;
    }

    /**
     * 通过 text 值找到对应的枚举
     *
     * @param text 枚举文本
     * @return 对应的枚举对象
     */
    public static MessageTypeEnum getEnumByText(String text) {
        if (ObjUtil.isEmpty(text)) {
            return null;
        }
        for (MessageTypeEnum messageTypeEnum : MessageTypeEnum.values()) {
            if (messageTypeEnum.text.equals(text)) {
                return messageTypeEnum;
            }
        }
        return null;
    }

}
