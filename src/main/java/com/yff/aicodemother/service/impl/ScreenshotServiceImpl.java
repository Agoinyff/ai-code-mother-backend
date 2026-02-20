package com.yff.aicodemother.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yff.aicodemother.exception.ErrorCode;
import com.yff.aicodemother.exception.ThrowUtils;
import com.yff.aicodemother.manager.CosManager;
import com.yff.aicodemother.model.entity.App;
import com.yff.aicodemother.service.AppService;
import com.yff.aicodemother.service.ScreenshotService;
import com.yff.aicodemother.utils.WebScreenshotUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author yff
 * @date 2026-02-20 09:47:45
 */
@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Autowired
    private CosManager cosManager;

    // 用 @Lazy 打破 AppService ↔ ScreenshotService 的循环依赖（AppService 可能也注入了
    // ScreenshotService）
    @Lazy
    @Autowired
    private AppService appService;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {

        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "网页URL不能为空");
        log.info("开始生成网页截图 - url: {}", webUrl);

        // 生成本地截图
        String localFilePath = WebScreenshotUtils.savaWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(localFilePath), ErrorCode.SYSTEM_ERROR, "生成网页截图失败");

        try {
            // 上传到对象存储
            String cosUrl = uploadScreenshotToCos(localFilePath);

            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.SYSTEM_ERROR, "上传网页截图失败");

            log.info("成功生成并上传网页截图 - url: {}, cosUrl: {}", webUrl, cosUrl);
            return cosUrl;
        } finally {

            // 清理本地文件
            cleanUpLocalFile(localFilePath);

        }

    }

    /**
     * 异步截图并更新应用封面。
     * 使用 @Async 在独立线程中执行，不阻塞调用方（StaticResourceController）。
     * 截图成功后将 COS URL 写入 App.cover 字段。
     *
     * @param appId      应用ID
     * @param url 要截图的 URL
     */
    @Override
    @Async
    public void captureAndUpdateCoverAsync(long appId, String url) {
        //这里返回的是对象存储的url
        String screenshotUrl = this.generateAndUploadScreenshot(url);

        //更新应用封面字段
        LambdaUpdateWrapper<App> appLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        appLambdaUpdateWrapper.eq(App::getId, appId)
                .set(App::getCover, screenshotUrl);
        boolean result = appService.update(appLambdaUpdateWrapper);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新应用封面失败");
    }

    /**
     * 清理本地文件，避免占用磁盘空间
     * 
     * @param localFilePath 要删除的本地文件路径
     */
    private void cleanUpLocalFile(String localFilePath) {
        File file = new File(localFilePath);

        if (file.exists()) {
            File parentFile = file.getParentFile();
            FileUtil.del(parentFile);
            log.info("已清理本地文件 - path: {}", localFilePath);
        }
    }

    /**
     * 上传截图到对象存储，并返回访问url
     * 
     * @param localFilePath 要上传的本地文件路径
     * @return 上传成功后的访问url，如果上传失败则返回null
     */
    private String uploadScreenshotToCos(String localFilePath) {

        if (StrUtil.isBlank(localFilePath)) {
            return null;
        }

        File file = new File(localFilePath);
        if (!file.exists()) {
            log.error("要上传的文件不存在 - path: {}", localFilePath);
            return null;
        }

        // 生成cos对象键
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compress.jpg";
        String key = generateScreenshotKey(fileName);
        return cosManager.uploadFile(key, file);

    }

    /*
     * 生成截图在对象存储中的唯一键（路径），可以根据需要自定义目录结构
     * 格式 /screenshots/yyyy/MM/dd/fileName.jpg
     */
    private String generateScreenshotKey(String fileName) {

        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s", datePath, fileName);

    }
}
