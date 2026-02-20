package com.yff.aicodemother.service;

/**
 * @author yff
 * @date 2026-02-20 09:44:52
 */
public interface ScreenshotService {


    /**
     * 生成指定网页的截图，并上传到对象存储，返回访问url
     * @param webUrl 要截图的网页URL
     * @return 截图上传成功后的访问URL，如果生成或上传失败则返回null
     */
    String generateAndUploadScreenshot(String webUrl);


}
