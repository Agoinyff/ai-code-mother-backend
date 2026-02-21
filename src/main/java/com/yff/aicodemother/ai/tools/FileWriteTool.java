package com.yff.aicodemother.ai.tools;


import com.yff.aicodemother.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 *
 * 文件写入工具
 * 支持使用AI的方式调用
 *
 *
 * @author yff
 * @date 2026-02-11 10:18:26
 */
@Slf4j
@Component
public class FileWriteTool {

    @Tool("写入文件到指定路径")
    public String writeFile(
            @P("文件的相对路径")
            String relativePath,
            @P("文件内容")
            String content,
            @ToolMemoryId     //这里提供appId作为唯一网站Id
            Long appId) {
        try {
            Path path = Paths.get(relativePath); //使用工具转换字符串路径为对象
            if (!path.isAbsolute()) {
                //判断是否是绝对路径，不是就进行处理，创建基于appId的项目目录
                String projectDirName = "vue_project_" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                path = projectRoot.resolve(relativePath);
            }
            //创建父目录（如果不存在）
            Path parentDir = path.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            //写入文件内容
            Files.write(path, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("成功写入文件：{}", path.toAbsolutePath());
            //返回相对路径
            return "文件写入成功：" + relativePath;
        } catch (IOException e) {
            String errorMessage = "文件写入失败：" + relativePath + ",错误：" + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }

    }


}
