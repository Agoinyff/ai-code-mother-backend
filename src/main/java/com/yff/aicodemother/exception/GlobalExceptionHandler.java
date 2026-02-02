package com.yff.aicodemother.exception;


import com.yff.aicodemother.common.BaseResponse;
import com.yff.aicodemother.common.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author yff
 * @date 2026-02-02 10:06:59
 * * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
@Hidden //隐藏在Swagger文档中
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)  //捕获 BusinessException 异常进行处理
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException: ", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)  //捕获其他未处理的异常进行处理
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException: ", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }



}
