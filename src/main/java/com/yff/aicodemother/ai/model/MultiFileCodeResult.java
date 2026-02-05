package com.yff.aicodemother.ai.model;


import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * @author yff
 * @date 2026-02-05 13:31:12
 */
@Description("包含生成的多文件代码及其描述的结果对象")
@Data
public class MultiFileCodeResult {

    @Description("生成的HTML代码")
    private String htmlCode;
    @Description("生成的CSS代码")
    private String cssCode;
    @Description("生成的JavaScript代码")
    private String jsCode;
    @Description("生成的多文件代码的描述")
    private String description;

}
