package com.yff.aicodemother.service;

/**
 * @author yff
 * @date 2026-02-20 09:44:52
 */
public interface ScreenshotService {

    /**
     * 生成指定网页的截图，并上传到对象存储，返回访问url
     * 
     * @param webUrl 要截图的网页URL
     * @return 截图上传成功后的访问URL，如果生成或上传失败则返回null
     */
    String generateAndUploadScreenshot(String webUrl);

    /**
     * 异步截图并更新应用封面（不阻塞调用方）。
     * 截图上传成功后自动将 COS URL 写入 App.cover 字段。
     *
     * @param appId      应用ID
     * @param previewUrl 要截图的预览 URL
     */
    void captureAndUpdateCoverAsync(long appId, String previewUrl);

}
