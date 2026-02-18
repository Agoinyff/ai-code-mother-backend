package com.yff.aicodemother.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部署历史记录实体
 * <p>
 * 记录每次 Docker 容器化部署的详细信息，支持版本管理和回滚。
 *
 * @author yff
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("deploy_history")
public class DeployHistory implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联应用ID
     */
    private Long appId;

    /**
     * 部署版本号（同一应用内自增）
     */
    private Integer version;

    /**
     * Docker 镜像标签（如 acm-deploy-123:v1708xxx）
     */
    private String imageTag;

    /**
     * Docker 容器ID
     */
    private String containerId;

    /**
     * 容器映射端口
     */
    private Integer containerPort;

    /**
     * 容器状态：RUNNING / STOPPED / FAILED
     */
    private String status;

    /**
     * 部署访问 URL
     */
    private String deployUrl;

    /**
     * 部署者用户ID
     */
    private Long userId;

    /**
     * 部署时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
