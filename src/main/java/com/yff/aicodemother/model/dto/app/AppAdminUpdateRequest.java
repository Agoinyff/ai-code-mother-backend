package com.yff.aicodemother.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员更新应用请求（支持更新名称、封面、优先级）
 *
 * @author yff
 * @since 2026-02-06
 */
@Data
public class AppAdminUpdateRequest implements Serializable {

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

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 优先级（99表示精选应用）
     */
    private Integer priority;

}
