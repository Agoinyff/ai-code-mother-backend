package com.yff.aicodemother.service.impl;

import cn.hutool.core.util.ZipUtil;
import com.yff.aicodemother.exception.BusinessException;
import com.yff.aicodemother.exception.ErrorCode;
import com.yff.aicodemother.service.ProjectDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * 项目下载服务实现类
 * <p>
 * 使用 Hutool {@link ZipUtil} 将源码目录打包为 ZIP，
 * 通过文件过滤器排除 node_modules、dist/build 构建产物、
 * 敏感配置文件等，并直接写入 HTTP 响应输出流。
 * </p>
 *
 * @author yff
 * @date 2026-02-20
 */
@Service
@Slf4j
public class ProjectDownloadServiceImpl implements ProjectDownloadService {

    /**
     * 需要从 ZIP 包中排除的目录名（精确匹配目录名或文件名前缀）
     */
    private static final Set<String> EXCLUDED_DIR_NAMES = Set.of(
            "node_modules",
            "dist",
            "build",
            ".git");

    /**
     * 需要排除的精确文件名
     */
    private static final Set<String> EXCLUDED_FILE_NAMES = Set.of(
            ".DS_Store",
            "Dockerfile",
            ".env",
            "Thumbs.db");

    /**
     * 需要排除的文件名前缀（.env.development、.env.production 等）
     */
    private static final String EXCLUDED_FILE_PREFIX = ".env.";

    /**
     * 需要排除的文件扩展名
     */
    private static final Set<String> EXCLUDED_EXTENSIONS = Set.of(
            ".log");

    @Override
    public void downloadAsZip(String sourceDirPath, String zipFileName, HttpServletResponse response) {
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用代码不存在，请先生成代码");
        }

        log.info("开始打包源码目录 - path: {}, zipFileName: {}", sourceDirPath, zipFileName);

        // 设置 HTTP 响应头
        response.setContentType("application/zip");
        response.setCharacterEncoding("UTF-8");
        // Content-Disposition: attachment 告知浏览器弹出下载框，并指定文件名
        String encodedFileName = URLEncoder.encode(zipFileName, StandardCharsets.UTF_8)
                .replace("+", "%20"); // 空格编码成 %20 而非 +，确保浏览器兼容
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
        // 暴露 Content-Disposition 给前端 JS（跨域场景下 fetch 需要此头）
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        // 构建文件过滤器，排除不需要打包的目录和文件
        FileFilter fileFilter = buildFileFilter();

        try (OutputStream outputStream = response.getOutputStream()) {
            // 使用 Hutool ZipUtil 打包目录并写入输出流，同时应用文件过滤器
            // withSrcDir=false: ZIP 内容为目录内的文件，不包含源码目录本身作为根目录
            ZipUtil.zip(outputStream, StandardCharsets.UTF_8, false, fileFilter, sourceDir);
            outputStream.flush();
            log.info("源码打包完成 - zipFileName: {}", zipFileName);
        } catch (IOException e) {
            log.error("打包源码时发生 IO 异常 - path: {}", sourceDirPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码下载失败：" + e.getMessage());
        }
    }

    /**
     * 构建文件过滤器，返回 true 表示该文件/目录需要被包含进 ZIP。
     *
     * @return 文件过滤器
     */
    private FileFilter buildFileFilter() {
        return file -> {
            String name = file.getName();

            // 目录：排除 node_modules、dist、build、.git 等
            if (file.isDirectory()) {
                boolean excluded = EXCLUDED_DIR_NAMES.contains(name);
                if (excluded) {
                    log.debug("过滤目录: {}", file.getAbsolutePath());
                }
                return !excluded;
            }

            // 文件：排除精确文件名（.DS_Store、.env）
            if (EXCLUDED_FILE_NAMES.contains(name)) {
                log.debug("过滤文件（精确匹配）: {}", file.getAbsolutePath());
                return false;
            }

            // 文件：排除 .env.* 前缀（.env.development、.env.production 等）
            if (name.startsWith(EXCLUDED_FILE_PREFIX)) {
                log.debug("过滤文件（.env.*）: {}", file.getAbsolutePath());
                return false;
            }

            // 文件：排除特定扩展名（.log 等）
            for (String ext : EXCLUDED_EXTENSIONS) {
                if (name.endsWith(ext)) {
                    log.debug("过滤文件（扩展名 {}）: {}", ext, file.getAbsolutePath());
                    return false;
                }
            }

            return true;
        };
    }
}
