package com.yff.aicodemother.model.dto.app;

import com.yff.aicodemother.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员分页查询应用请求（支持除时间外的任何字段查询，每页数量不限）
 *
 * @author yff
 * @since 2026-02-06
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppAdminQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用id
     */
    private Long id;

    /**
     * 应用名称（模糊查询）
     */
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 应用初始化的 prompt
     */
    private String initPrompt;

    /**
     * 代码生成类型
     */
    private String codeGenType;

    /**
     * 部署标识
     */
    private String deployKey;

    /**
     * 优先级（99表示精选应用）
     */
    private Integer priority;

    /**
     * 创建用户id
     */
    private Long userId;

}
