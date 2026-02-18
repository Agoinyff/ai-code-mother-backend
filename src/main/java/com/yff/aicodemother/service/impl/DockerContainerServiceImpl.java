package com.yff.aicodemother.service.impl;

import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import com.yff.aicodemother.constant.AppConstant;
import com.yff.aicodemother.exception.BusinessException;
import com.yff.aicodemother.exception.ErrorCode;
import com.yff.aicodemother.service.DockerContainerService;
import com.yff.aicodemother.service.DockerPortManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Docker 容器管理服务实现
 * <p>
 * 核心职责：
 * 1. 为 VUE_PROJECT 创建预览容器（Node dev server，临时）
 * 2. 为所有类型创建部署容器（Nginx 静态服务，持久）
 * 3. 管理容器生命周期（启动、停止、清理过期容器）
 *
 * @author yff
 */
@Slf4j
@Service
public class DockerContainerServiceImpl implements DockerContainerService {

    @Autowired
    private DockerClient dockerClient;

    @Autowired
    private DockerPortManager portManager;

    /**
     * Docker 宿主机的访问地址（用于生成预览/部署 URL）
     * 本地开发时为 localhost，云服务器时为公网 IP
     */
    @Value("${docker.preview-host:localhost}")
    private String previewHost;


    /**
     * 部署容器信息缓存：appId -> ContainerInfo
     */
    private final ConcurrentHashMap<Long, ContainerInfo> deployContainers = new ConcurrentHashMap<>();

    /**
     * 容器信息内部类
     */
    private record ContainerInfo(
            String containerId,
            int port,
            Instant createdAt) {
    }

    /**
     * 启动时扫描已存在的容器，恢复端口池状态
     */
    @PostConstruct
    public void init() {
        if (!isDockerAvailable()) {
            log.warn("Docker 不可用，跳过容器状态恢复");
            return;
        }
        try {
            recoverExistingContainers();
        } catch (Exception e) {
            log.warn("恢复已有容器状态失败: {}，将忽略历史容器", e.getMessage());
        }
    }

    // ==================== 部署容器管理 ====================

    @Override
    public String deployAsContainer(Long appId) {
        // 1. 获取项目源代码路径（需要根据类型判断是用源文件还是 dist 目录）
        String sourceDirPath = resolveDeploySourceDir(appId);
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署源目录不存在: " + sourceDirPath);
        }

        // 2. 复制 Nginx Dockerfile 到源目录
        copyDockerfileTemplate(sourceDir, "Dockerfile.nginx");

        // 3. 如果有旧的部署容器，先停止
        ContainerInfo existing = deployContainers.get(appId);
        int port;
        if (existing != null) {
            // 复用旧端口（保持 URL 不变）
            port = existing.port();
            stopAndRemoveContainer(existing.containerId(), "旧部署");
        } else {
            port = portManager.allocateDeployPort();
        }

        try {
            // 4. 构建部署镜像
            long version = System.currentTimeMillis();
            String imageTag = "acm-deploy-" + appId + ":v" + version;
            log.info("开始构建部署镜像: {}", imageTag);

            String imageId = dockerClient.buildImageCmd(sourceDir)
                    .withDockerfile(new File(sourceDir, "Dockerfile"))
                    .withTags(Set.of(imageTag))
                    .exec(new BuildImageResultCallback())
                    .awaitImageId();

            log.info("部署镜像构建完成: {}", imageId);

            // 5. 创建并启动容器
            CreateContainerResponse container = dockerClient.createContainerCmd(imageTag)
                    .withName("acm-deploy-" + appId)
                    .withHostConfig(HostConfig.newHostConfig()
                            .withPortBindings(PortBinding.parse(port + ":80"))
                            .withMemory(256 * 1024 * 1024L) // 限制内存 256MB（Nginx 较轻量）
                            .withCpuCount(1L)
                            .withRestartPolicy(RestartPolicy.alwaysRestart()) // 异常退出自动重启
                    )
                    .withExposedPorts(ExposedPort.tcp(80))
                    .withLabels(Map.of(
                            AppConstant.DOCKER_LABEL_PREFIX, AppConstant.DOCKER_LABEL_DEPLOY,
                            "appId", String.valueOf(appId),
                            "version", String.valueOf(version)))
                    .exec();

            dockerClient.startContainerCmd(container.getId()).exec();

            // 6. 记录部署容器信息
            deployContainers.put(appId, new ContainerInfo(container.getId(), port, Instant.now()));

            String deployUrl = String.format("http://%s:%d", previewHost, port);
            log.info("部署容器启动成功 - 应用:{}, 容器:{}, 镜像:{}, URL:{}", appId, container.getId(), imageTag, deployUrl);
            return deployUrl;

        } catch (Exception e) {
            if (existing == null) {
                portManager.releaseDeployPort(port); // 只在新分配的端口时归还
            }
            log.error("容器化部署失败 - 应用:{}", appId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "容器化部署失败: " + e.getMessage());
        }
    }

    @Override
    public void stopDeployContainer(Long appId) {
        ContainerInfo info = deployContainers.remove(appId);
        if (info == null) {
            log.info("应用 {} 没有运行中的部署容器", appId);
            return;
        }
        stopAndRemoveContainer(info.containerId(), "部署");
        portManager.releaseDeployPort(info.port());
    }

    @Override
    public boolean isDockerAvailable() {
        try {
            dockerClient.pingCmd().exec();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    // ==================== 私有方法 ====================

    /**
     * 解析部署源目录路径
     * HTML 类型直接使用 code_output 目录
     * Vue 项目需要先 build 后使用 dist 目录
     */
    private String resolveDeploySourceDir(Long appId) {
        // 先尝试 Vue 项目的 dist 目录
        String vueDistPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + "vue_project_" + appId + File.separator
                + "dist";
        if (new File(vueDistPath).exists()) {
            return vueDistPath;
        }
        // 尝试 HTML 项目目录
        String htmlPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + "html_" + appId;
        if (new File(htmlPath).exists()) {
            return htmlPath;
        }
        // 尝试 multi_file 项目目录
        String multiFilePath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + "multi_file_" + appId;
        if (new File(multiFilePath).exists()) {
            return multiFilePath;
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "未找到应用 " + appId + " 的代码目录");
    }

    /**
     * 复制 Dockerfile 模板到目标目录
     */
    private void copyDockerfileTemplate(File targetDir, String templateName) {
        try {
            InputStream templateStream = getClass().getClassLoader()
                    .getResourceAsStream("docker/" + templateName);
            if (templateStream == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Dockerfile 模板不存在: " + templateName);
            }
            File dockerfile = new File(targetDir, "Dockerfile");
            Files.copy(templateStream, dockerfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            templateStream.close();
            log.info("Dockerfile 模板已复制到: {}", dockerfile.getAbsolutePath());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "复制 Dockerfile 模板失败: " + e.getMessage());
        }
    }

    /**
     * 停止并删除容器
     */
    private void stopAndRemoveContainer(String containerId, String type) {
        try {
            dockerClient.stopContainerCmd(containerId).withTimeout(10).exec();
            log.info("{}容器已停止: {}", type, containerId);
        } catch (Exception e) {
            log.warn("停止{}容器失败（可能已停止）: {}, 原因: {}", type, containerId, e.getMessage());
        }
        try {
            dockerClient.removeContainerCmd(containerId).withForce(true).exec();
            log.info("{}容器已删除: {}", type, containerId);
        } catch (Exception e) {
            log.warn("删除{}容器失败: {}, 原因: {}", type, containerId, e.getMessage());
        }
    }

    /**
     * 系统启动时恢复已存在的容器端口状态
     */
    private void recoverExistingContainers() {
        List<Container> containers = dockerClient.listContainersCmd()
                .withLabelFilter(Map.of(AppConstant.DOCKER_LABEL_PREFIX, ""))
                .withShowAll(true)
                .exec();

        for (Container container : containers) {
            Map<String, String> labels = container.getLabels();
            if (labels == null)
                continue;

            String appIdStr = labels.get("appId");
            String type = labels.get(AppConstant.DOCKER_LABEL_PREFIX);
            if (appIdStr == null || type == null)
                continue;

            Long appId = Long.valueOf(appIdStr);

            // 获取映射端口
            ContainerPort[] ports = container.getPorts();
            if (ports == null || ports.length == 0)
                continue;

            for (ContainerPort cp : ports) {
                Integer publicPort = cp.getPublicPort();
                if (publicPort == null)
                    continue;

                if (AppConstant.DOCKER_LABEL_DEPLOY.equals(type)) {
                    portManager.markDeployPortAsUsed(publicPort);
                    if ("running".equals(container.getState())) {
                        deployContainers.put(appId, new ContainerInfo(container.getId(), publicPort, Instant.now()));
                    }
                }
                break; // 只取第一个端口映射
            }
        }

        log.info("容器状态恢复完成 - 部署容器: {}",
                deployContainers.size());
    }
}
