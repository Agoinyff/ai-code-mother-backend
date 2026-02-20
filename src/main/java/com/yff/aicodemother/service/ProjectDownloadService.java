package com.yff.aicodemother.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 项目下载通用服务
 * <p>
 * 将指定目录的源码文件过滤后打包为 ZIP，写入 HTTP 响应以供下载。
 * 类比 ScreenshotService，作为独立的通用工具服务单独封装。
 * </p>
 *
 * @author yff
 * @date 2026-02-20
 */
public interface ProjectDownloadService {

    /**
     * 将指定路径下的源码文件打包为 ZIP，并写入 HttpServletResponse 供前端下载。
     * <p>
     * 会自动过滤 node_modules、dist、build、.env、.DS_Store 等不需要下载的文件。
     * </p>
     *
     * @param sourceDirPath 要打包的源码目录绝对路径
     * @param zipFileName   下载时展示的文件名（如 my-app.zip）
     * @param response      HTTP 响应对象，ZIP 内容将直接写入其输出流
     */
    void downloadAsZip(String sourceDirPath, String zipFileName, HttpServletResponse response);
}
