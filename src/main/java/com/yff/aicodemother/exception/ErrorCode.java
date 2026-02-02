package com.yff.aicodemother.exception;

import lombok.Getter;

/**
 * @author yff
 * @date 2026-02-02 09:44:05
 */
@Getter
public enum ErrorCode {


    SUCCESS(0, "成功"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求资源不存在"),
    FORBIDDEN_ERROR(40300, "请求被拒绝"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    CAPTCHA_ERROR(50001, "验证码错误"),
    RATE_LIMIT_ERROR(50002, "请求过于频繁，请稍后再试"),
    THIRD_PARTY_ERROR(60000, "第三方服务异常"),
    AI_SERVICE_ERROR(70000, "AI服务异常");




    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }


}
