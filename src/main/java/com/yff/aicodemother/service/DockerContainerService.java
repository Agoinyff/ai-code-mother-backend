package com.yff.aicodemother.service;

/**
 * Docker 容器管理服务接口
 * <p>
 * 提供容器化预览和部署的核心能力：
 * - 创建/停止预览容器（临时，30分钟自动清理）
 * - 创建/停止部署容器（持久，支持版本管理和回滚）
 *
 * @author yff
 */
public interface DockerContainerService {




    /**
     * 容器化部署应用
     * <p>
     * 将生成的代码构建为 Docker 镜像（基于 Nginx），启动持久容器提供访问。
     * 每次部署生成新版本，旧版本容器停止但保留镜像（用于回滚）。
     *
     * @param appId 应用ID
     * @return 部署后的访问 URL
     */
    String deployAsContainer(Long appId);

    /**
     * 停止指定应用的部署容器
     *
     * @param appId 应用ID
     */
    void stopDeployContainer(Long appId);

    /**
     * 检查 Docker Engine 是否可用
     *
     * @return true 如果 Docker Engine 连接正常
     */
    boolean isDockerAvailable();

}
