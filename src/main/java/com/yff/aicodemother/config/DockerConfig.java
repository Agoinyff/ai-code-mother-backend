package com.yff.aicodemother.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Docker 客户端配置类
 * <p>
 * 提供 DockerClient Bean，支持连接本地或远程 Docker Engine。
 * 通过 application.yml 中的 docker.host 配置 Docker 地址：
 * - 本地 Windows: npipe:////./pipe/docker_engine
 * - 本地 Linux: unix:///var/run/docker.sock
 * - 远程服务器: tcp://your-server-ip:2375
 *
 * @author yff
 */
@Slf4j
@Configuration
public class DockerConfig {

    @Value("${docker.host:tcp://localhost:2375}")
    private String dockerHost;

    @Value("${docker.api-version:1.41}")
    private String apiVersion;

    @Value("${docker.connect-timeout:5}")
    private int connectTimeoutSeconds;

    @Value("${docker.read-timeout:30}")
    private int readTimeoutSeconds;

    @Bean
    public DockerClient dockerClient() {
        log.info("初始化 Docker 客户端，连接地址: {}", dockerHost);

        // 1. 构建 Docker 客户端配置
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .withApiVersion(apiVersion)
                .build();

        // 2. 构建 HTTP 客户端（用于与 Docker Engine 通信）
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .responseTimeout(Duration.ofSeconds(readTimeoutSeconds))
                .build();

        // 3. 创建 DockerClient 实例
        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

        // 4. 验证连接（测试 ping）
        try {
            dockerClient.pingCmd().exec();
            log.info("Docker 客户端连接成功！Docker Engine 状态正常");
        } catch (Exception e) {
            log.warn("Docker 客户端连接失败: {}，容器化功能将不可用。请检查 Docker 服务是否启动。", e.getMessage());
        }

        return dockerClient;
    }
}
