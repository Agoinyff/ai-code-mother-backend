package com.yff.aicodemother.ai.core.builder;

import cn.hutool.core.util.RuntimeUtil;


import cn.hutool.core.util.RuntimeUtil;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author yff
 * @date 2026-02-12 15:56:42
 */
@Slf4j
@Component
public class VueProjectBuilder {


    /**
     * 使用线程异步构建Vue项目
     *
     * @param projectPath 项目路径
     */
    @Async
    public void buildProjectAsync(String projectPath) {
        //在单独的线程中执行构建任务，避免阻塞主线程
            try {
                buildProject(projectPath);
            } catch (Exception e) {
                log.error("构建Vue项目时发生异常: {}", e.getMessage(), e);
            }

    }


    /**
     * 构建Vue项目
     *
     * @param projectPath 项目路径
     * @return 构建是否成功
     */
    public boolean buildProject(String projectPath) {
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录不存在或不是目录: {}", projectPath);
            return false;
        }

        //检查package.json文件是否存在
        File packageJson = new File(projectDir, "package.json");
        if (!packageJson.exists()) {
            log.error("package.json文件不存在: {}", packageJson.getAbsolutePath());
            return false;
        }

        //执行npm install
        if (!executeNpmInstall(projectDir)) {
            log.error("npm install命令执行失败");
            return false;
        }
        //执行npm run build
        if (!executeNpmBuild(projectDir)) {
            log.error("npm run build命令执行失败");
            return false;
        }

        //验证dist目录是否生成
        File distDir = new File(projectDir, "dist");
        if (!distDir.exists()) {
            log.error("构建失败，dist目录未生成: {}", distDir.getAbsolutePath());
            return false;
        } else {
            log.info("项目构建成功，dist目录已生成: {}", distDir.getAbsolutePath());
            return true;
        }

    }

    /**
     * 执行命令行指令的通用方法
     *
     * @param workingDir     工作目录
     * @param command        命令
     * @param timeoutSeconds 超时时间，单位秒
     * @return 命令执行是否成功
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {

        try {
            log.info("在目录{}中执行命令：{}", workingDir.getAbsolutePath(), command);
            Process process = RuntimeUtil.exec(null, workingDir, command.split("\\s+"));//命令分割为数组

            //等待进程完成，设置超时
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令执行超时：{},强制终止进程", command);
                process.destroyForcibly(); // 超时后销毁进程
                return false;
            }
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功：{}", command);
                return true;
            } else {
                log.error("命令执行失败：{}，退出码：{}", command, exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令出错：{},错误信息:{}", command, e.getMessage());
            return false;
        }


    }


    /**
     * 执行npm install命令安装依赖
     *
     * @param projectDir 项目目录
     * @return 命令执行是否成功
     */
    private boolean executeNpmInstall(File projectDir) {
        log.info("开始执行npm install命令安装依赖...");
        String command = String.format("%s install", buildCommand("npm"));
        return executeCommand(projectDir, command, 300);
    }


    /**
     * 执行npm run build命令构建项目
     *
     * @param projectDir 项目目录
     * @return 命令执行是否成功
     */
    private boolean executeNpmBuild(File projectDir) {
        log.info("开始执行npm run build命令构建项目...");
        String command = String.format("%s run build", buildCommand("npm"));
        return executeCommand(projectDir, command, 180);
    }

    /**
     * 根据操作系统构建命令
     *
     * @param baseCommand 基础命令
     * @return 适配当前操作系统的命令
     */
    private String buildCommand(String baseCommand) {
        if (isWindows()) {
            return baseCommand + ".cmd";
        }
        return baseCommand;
    }

    /**
     * 判断当前操作系统是否为Windows
     *
     * @return true 如果是Windows系统，否则false
     */
    private boolean isWindows() {

        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
}

