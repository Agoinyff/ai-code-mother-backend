package com.yff.aicodemother.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class WebScreenshotUtilsTest {





    @Test
    void saveWebPageScreenshotTest(){

        String testUrl = "https://www.baidu.com";
        String s = WebScreenshotUtils.savaWebPageScreenshot(testUrl);
        Assertions.assertNotNull(s);



    }


}