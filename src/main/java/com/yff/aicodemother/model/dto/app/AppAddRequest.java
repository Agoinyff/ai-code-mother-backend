package com.yff.aicodemother.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建应用请求
 *
 * @author yff
 * @since 2026-02-06
 */
@Data
public class AppAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 应用初始化的 prompt（必填）
     */
    private String initPrompt;

    /**
     * 代码生成类型
     */
    private String codeGenType;

}
