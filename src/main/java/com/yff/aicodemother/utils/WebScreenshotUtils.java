package com.yff.aicodemother.utils;


import cn.hutool.core.img.Img;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.yff.aicodemother.exception.BusinessException;
import com.yff.aicodemother.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;

/**
 *
 * 初始化webdriver驱动
 *
 * @author yff
 * @date 2026-02-19 14:21:29
 */

@Slf4j
public class WebScreenshotUtils {

    private static final WebDriver webDriver;


    //使用静态代码块初始化 WebDriver，确保在类加载时就完成驱动的设置
    static {
        final int DEFAULT_WIDTH = 1600;
        final int DEFAULT_HEIGHT = 900;
        webDriver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }


    @PreDestroy
    public void destroy() {
        webDriver.close();
    }


    public static WebDriver initChromeDriver(int width, int height) {
        try {
            // 设置 ChromeDriver 的系统属性，WebDriverManager 会自动下载和配置适合当前系统的 ChromeDriver 版本
            WebDriverManager.chromedriver().setup();
            // 创建 ChromeOptions 对象，可以在这里添加一些启动参数，例如无头模式、禁用 GPU 等
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--headless"); // 无头模式
            chromeOptions.addArguments("--disable-gpu"); // 禁用 GPU 加速
            chromeOptions.addArguments("--no-sandbox"); // 禁用沙盒模式  (docker 环境下需要)
            chromeOptions.addArguments("--disable-dev-shm-usage"); // 禁用 开发者shm调用
            chromeOptions.addArguments(String.format("--window-size=%d,%d", width, height)); // 设置窗口大小
            chromeOptions.addArguments("--disable-extensions"); // 禁用扩展
            //设置用户代理
            chromeOptions.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            ChromeDriver driver = new ChromeDriver(chromeOptions); //创建驱动
            //设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            //设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化chrome浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化chrome浏览器失败");
        }

    }


    //保存图片到文件
    private static void saveImage(byte[] imageBytes, String imagePath) {


        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (IORuntimeException e) {
            log.error("保存图片失败:{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }

    }

    //压缩图片
    private static void compressImage(String originalImagePath, String compressedImagePath) {
        final float COMPRESS_QUALITY = 0.3f; // 压缩质量，范围0-1，数值越小压缩率越高
        try {
            ImgUtil.compress(
                    FileUtil.file(originalImagePath),
                    FileUtil.file(compressedImagePath),
                    COMPRESS_QUALITY
            );
        } catch (Exception e) {
            log.error("压缩图片失败:{} -> {}", originalImagePath, compressedImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }


    //等待页面加载完成再截图
    private static void waitForPageLoad(WebDriver driver) {
        try {
            //创建等待对象
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            //等待页面加载完成，直到 document.readyState 为 complete   等待dom加载完成
            wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));

            //额外等待一段时间,确保页面上的动态内容也加载完成
            Thread.sleep(2000);
            log.info("页面加载完成");
        } catch (InterruptedException e) {
            log.error("页面加载的时候出现异常,继续执行截图操作");
        }

    }


    public static String savaWebPageScreenshot(String webUrl) {
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页url不能为空");
            return null;
        }

        try {
            //创建临时目录
            String rootPath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenshots"
                    + File.separator + UUID.randomUUID().toString().substring(0, 8);
            FileUtil.mkdir(rootPath);

            //图片后缀
            final String IMAGE_SUFFIX = ".png";
            //原始截图路径
            String originalImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;

            //访问网页
            webDriver.get(webUrl);

            //等待页面加载完成
            waitForPageLoad(webDriver);

            //截图并保存原始图片
            byte[] screenshotBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            saveImage(screenshotBytes, originalImagePath);
            log.info("原始截图保存成功 {}", originalImagePath);

            //压缩图片
            final String COMPRESSION_SUFFIX = "_compressed.png";
            String compressedImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + COMPRESSION_SUFFIX;
            compressImage(originalImagePath, compressedImagePath);
            log.info("压缩截图保存成功 {}", compressedImagePath);
            //删除原始图片
            FileUtil.del(originalImagePath);
            return compressedImagePath;
        } catch (Exception e) {
            log.error("网页截图失败:{}", webUrl, e);
            return null;
        }
    }


}
