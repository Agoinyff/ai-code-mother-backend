package com.yff.aicodemother.common;


import com.yff.aicodemother.exception.ErrorCode;

/**
 * @author yff
 * @date 2026-02-02 10:02:32
 */
public class ResultUtils {

    /**
     * 成功响应
     * @param data  数据
     * @return BaseResponse<T>
     * @param <T> 数据类型
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, "success", data);
    }

    /**
     * 错误响应
     * @param code 错误码
     * @param message 错误信息
     * @return BaseResponse<T>
     * @param <T> 数据类型
     */
    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, message, null);
    }


    /**
     * 错误响应
     * @param errorCode 错误码枚举
     * @return BaseResponse<T>
     * @param <T> 数据类型
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 错误响应
     * @param errorCode 错误码枚举
     * @param message 错误信息
     * @return BaseResponse<T>
     * @param <T> 数据类型
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), message, null);
    }


}
