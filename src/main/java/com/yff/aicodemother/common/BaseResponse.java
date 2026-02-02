package com.yff.aicodemother.common;


import com.yff.aicodemother.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yff
 * @date 2026-02-02 09:59:23
 */
@Data
public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;

    private String message;

    private T data;

    public BaseResponse() {
    }

    public BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(int code, T data) {
        this(code, null, data);
    }

    public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(), errorCode.getMessage(), null);
    }


}
