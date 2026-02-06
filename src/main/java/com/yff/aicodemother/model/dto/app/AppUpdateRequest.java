package com.yff.aicodemother.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户更新自己的应用请求（仅支持修改名称）
 *
 * @author yff
 * @since 2026-02-06
 */
@Data
public class AppUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

}
