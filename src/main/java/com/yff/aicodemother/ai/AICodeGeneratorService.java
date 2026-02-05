package com.yff.aicodemother.ai;


import com.yff.aicodemother.ai.model.HtmlCodeResult;
import com.yff.aicodemother.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author yff
 * @date 2026-02-05 10:24:53
 */
public interface AICodeGeneratorService {


    String generateCode(String userMessage);

    /**
     * 生成HTML代码
     *
     * @param userMessage 用户输入的信息
     * @return 生成的HTML代码
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成多文件代码
     *
     * @param userMessage 用户输入的信息
     * @return 生成的多文件代码，使用特定格式进行分隔
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);


    //以下是新增的流式方法


    /**
     * 流式生成HTML代码
     *
     * @param userMessage 用户输入的信息
     * @return 生成的HTML代码流
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 流式生成多文件代码
     *
     * @param userMessage 用户输入的信息
     * @return 生成的多文件代码流，使用特定格式进行分隔
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);


}
