package com.yff.aicodemother.ai;

import com.yff.aicodemother.ai.model.HtmlCodeResult;
import com.yff.aicodemother.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {



    @Autowired
    private AICodeGeneratorService aiCodeGeneratorService;

    @Test
    void GenerateHtmlCode(){
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode("请帮我生成一个简单的个人简介网页代码，包含姓名、照片和自我介绍。");
        Assertions.assertNotNull(htmlCodeResult);
    }

    @Test
    void GenerateMultiFileCode(){
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode("请帮我生成一个留言板页面");
        Assertions.assertNotNull(multiFileCodeResult);
    }



}