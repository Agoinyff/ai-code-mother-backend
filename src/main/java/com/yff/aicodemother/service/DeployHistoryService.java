package com.yff.aicodemother.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yff.aicodemother.model.entity.DeployHistory;

import java.util.List;

/**
 * 部署历史 服务层接口
 *
 * @author yff
 */
public interface DeployHistoryService extends IService<DeployHistory> {

    /**
     * 记录一次新的部署
     *
     * @param appId         应用ID
     * @param imageTag      Docker 镜像标签
     * @param containerId   容器ID
     * @param containerPort 映射端口
     * @param deployUrl     访问 URL
     * @param userId        部署者ID
     * @return 部署历史记录（含自动生成的版本号）
     */
    DeployHistory recordDeploy(Long appId, String imageTag, String containerId,
            Integer containerPort, String deployUrl, Long userId);

    /**
     * 获取某应用的部署历史列表（按版本号降序）
     *
     * @param appId 应用ID
     * @return 部署历史列表
     */
    List<DeployHistory> getDeployVersions(Long appId);

    /**
     * 更新部署记录状态
     *
     * @param id     部署历史记录ID
     * @param status 新状态（RUNNING / STOPPED / FAILED）
     */
    void updateStatus(Long id, String status);

    /**
     * 获取某应用的当前运行中的部署记录
     *
     * @param appId 应用ID
     * @return 运行中的部署记录，没有则返回 null
     */
    DeployHistory getRunningDeploy(Long appId);
}
