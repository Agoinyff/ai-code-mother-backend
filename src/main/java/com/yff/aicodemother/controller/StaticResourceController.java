package com.yff.aicodemother.controller;

import com.yff.aicodemother.constant.AppConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import java.io.File;

/**
 * 静态资源控制器 - 提供部署目录的静态文件访问服务
 * <p>
 * 访问格式: http://localhost/{deployKey}/
 * 示例: http://localhost/ZmNZys/ → 返回 /tmp/code_deploy/ZmNZys/index.html
 *
 * @author yff
 * @since 2026-02-07
 */
@RestController
@RequestMapping("/static")
@Tag(name = "静态资源服务", description = "提供部署目录的静态文件访问")
public class StaticResourceController {

    // 应用生成根目录（修改为从 code_output 读取，这样生成后可以立即预览）
    private static final String PREVIEW_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 提供静态资源访问，支持目录重定向
     *
     * @param deployKey 部署标识
     * @param request   HTTP请求对象
     * @return 文件资源
     */
    @GetMapping("/{deployKey}/**")
    @Operation(summary = "访问部署的静态资源")
    public ResponseEntity<Resource> serveStaticResource(
            @PathVariable String deployKey,
            HttpServletRequest request) {

        try {
            // 获取资源路径
            String resourcePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            resourcePath = resourcePath.substring(("/static/" + deployKey).length());
            // 如果是访问根目录，重定向到带斜杠的URL
            if (resourcePath.isEmpty()) {
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.add("Location", request.getRequestURI() + "/");
                return new ResponseEntity<>(httpHeaders, HttpStatus.MOVED_PERMANENTLY);
            }
            // 默认返回index.html
            if (resourcePath.equals("/")) {
                resourcePath = "/index.html";
            }

            String baseDir = PREVIEW_ROOT_DIR + File.separator + deployKey;

            // 优先从 dist 子目录查找（VUE_PROJECT 构建产物在 dist/ 下）
            // 这样可以避免返回 Vue 源码目录下的开发版 index.html（含 /src/main.js）
            File file = new File(baseDir + File.separator + "dist" + resourcePath);

            // dist 子目录找不到时，再从根目录查找（HTML 单页项目直接在根目录）
            if (!file.exists() || !file.isFile()) {
                File rootFile = new File(baseDir + resourcePath);
                if (rootFile.exists() && rootFile.isFile()) {
                    file = rootFile;
                }
            }

            // 检查文件是否存在
            if (!file.exists() || !file.isFile()) {
                // Vue Router history 模式：找不到文件时回退到 dist/index.html
                File indexFile = new File(baseDir + File.separator + "dist" + File.separator + "index.html");
                if (!indexFile.exists()) {
                    indexFile = new File(baseDir + File.separator + "index.html");
                }
                if (indexFile.exists()) {
                    FileSystemResource resource = new FileSystemResource(indexFile);
                    return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(resource);
                }
                return ResponseEntity.notFound().build();
            }

            // 返回文件资源
            FileSystemResource resource = new FileSystemResource(file);
            return ResponseEntity.ok().header("Content-Type", getContentType(file.getName())).body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据文件扩展名返回对应的 Content-Type
     *
     * @param filePath 文件路径
     * @return Content-Type 值
     */
    private String getContentType(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".html") || lowerPath.endsWith(".htm")) {
            return "text/html; charset=UTF-8";
        }
        if (lowerPath.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }
        if (lowerPath.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }
        if (lowerPath.endsWith(".json")) {
            return "application/json; charset=UTF-8";
        }
        if (lowerPath.endsWith(".png")) {
            return "image/png";
        }
        if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lowerPath.endsWith(".gif")) {
            return "image/gif";
        }
        if (lowerPath.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (lowerPath.endsWith(".ico")) {
            return "image/x-icon";
        }
        if (lowerPath.endsWith(".woff") || lowerPath.endsWith(".woff2")) {
            return "font/woff2";
        }
        if (lowerPath.endsWith(".ttf")) {
            return "font/ttf";
        }
        return "application/octet-stream";
    }

}
