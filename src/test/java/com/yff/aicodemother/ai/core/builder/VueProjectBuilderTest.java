package com.yff.aicodemother.ai.core.builder;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VueProjectBuilderTest {


    @Autowired
    private VueProjectBuilder vueProjectBuilder;


    @Test
    void vueProjectBuilderTest(){


        vueProjectBuilder.buildProjectAsync("D:\\javastudent\\ai-code-mother\\tmp\\code_output\\vue_project2021220052691808259");


    }


}