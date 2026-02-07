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



}
