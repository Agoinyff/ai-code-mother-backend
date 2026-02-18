package com.yff.aicodemother.constant;

/**
 * @author yff
 * @date 2026-02-07 14:19:55
 */
public interface AppConstant {

    /**
     * 应用生成目录
     */
    String CODE_OUTPUT_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 应用部署目录
     */
    String CODE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_deploy";

    /**
     * 应用部署域名
     */
    String CODE_DEPLOY_HOST = "http://localhost";

    // ==================== Docker 容器化相关常量 ====================



    /**
     * 部署容器端口范围起始
     */
    int DEPLOY_PORT_RANGE_START = 4001;

    /**
     * 部署容器端口范围结束
     */
    int DEPLOY_PORT_RANGE_END = 4100;



    /**
     * Docker 容器标签前缀（用于标识本系统创建的容器）
     */
    String DOCKER_LABEL_PREFIX = "ai-code-mother";

    /**
     * Docker 部署容器标签值
     */
    String DOCKER_LABEL_DEPLOY = "deploy";

}
