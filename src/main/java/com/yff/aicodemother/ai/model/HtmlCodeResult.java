package com.yff.aicodemother.ai.model;


import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * @author yff
 * @date 2026-02-05 13:30:13
 */
@Description("包含生成的HTML代码及其描述的结果对象")  //这里使用的是langchain4j的Description注解  为结果和字段添加描述信息
@Data
public class HtmlCodeResult {

    @Description("生成的HTML代码")
    private String htmlCode;

    @Description("生成的HTML代码的描述")
    private String description;


}
