package com.yff.aicodemother.ai.core;


import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.yff.aicodemother.ai.AICodeGeneratorService;
import com.yff.aicodemother.ai.AiCodeGeneratorServiceFactory;
import com.yff.aicodemother.ai.core.parser.CodeParserExecutor;
import com.yff.aicodemother.ai.core.saver.CodeFileSaverExecutor;
import com.yff.aicodemother.ai.model.HtmlCodeResult;
import com.yff.aicodemother.ai.model.MultiFileCodeResult;
import com.yff.aicodemother.ai.model.enums.CodeGenTypeEnum;
import com.yff.aicodemother.ai.model.message.AiResponseMessage;
import com.yff.aicodemother.ai.model.message.ToolExecutedMessage;
import com.yff.aicodemother.ai.model.message.ToolRequestMessage;
import com.yff.aicodemother.exception.BusinessException;
import com.yff.aicodemother.exception.ErrorCode;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * @author yff
 * @date 2026-02-05 14:21:58
 * <p>
 * AI生成代码门面类，组合生成和保存功能，提供统一的代码生成接口
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {


//    @Autowired
//    private AICodeGeneratorService aiCodeGeneratorService;

    @Autowired
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 统一入口：根据类型生成并保存代码文件
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 代码生成类型枚举
     * @return 保存的目录对象
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        AICodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);

        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }

        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(htmlCodeResult, CodeGenTypeEnum.HTML, appId); //yield关键字用于返回值

            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMsg = String.format("不支持的生成类型：%s", codeGenTypeEnum.getValue());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMsg);
            }
        };


    }

    /**
     * 统一入口：根据类型生成并保存代码文件(流式)
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 代码生成类型枚举
     * @return 生成的代码流
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        AICodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);


        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }

        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
            }

            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMsg = String.format("不支持的生成类型：%s", codeGenTypeEnum.getValue());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMsg);
            }
        };


    }

    /**
     * 通用流式代码处理方法
     *
     * @param codeStream      代码流
     * @param codeGenTypeEnum 代码生成类型枚举
     * @return 处理后的代码流
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum, Long appId) {

        StringBuilder codeBuilder = new StringBuilder();
        return codeStream
                .doOnNext(
                        chunk -> {
                            //在这里可以处理每个代码块的逻辑，例如拼接完整内容
                            codeBuilder.append(chunk);
                        }
                )
                .doOnComplete(
                        //在流式传输完成后执行保存操作
                        () -> {
                            try {
                                String completeCode = codeBuilder.toString();
                                log.info("完整多文件代码内容：\n{}", completeCode);
//                                MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeCode); //通过解析器获取完整代码结果对象
                                Object parseResult = CodeParserExecutor.executeParse(completeCode, codeGenTypeEnum);//使用解析器解析代码
//                                File savedDir = CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);//保存文件
                                File savedDir = CodeFileSaverExecutor.executeSaver(parseResult, codeGenTypeEnum, appId);//使用保存器保存代码文件
                                log.info("保存到目录：{}", savedDir.getAbsolutePath());
                            } catch (Exception e) {
                                log.error("保存失败：{}", e.getMessage());
                            }
                        }
                );
    }


    /**
     * 处理TokenStream流 转换为Flux 并传递工具调用信息
     *
     * @param tokenStream TokenStream流
     * @return 处理后的字符串流
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {

        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                sink.next(JSONUtil.toJsonStr(aiResponseMessage));
            }).onPartialToolCall((toolExecution) -> {
                ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecution);
                sink.next(JSONUtil.toJsonStr(toolRequestMessage));
            }).onToolExecuted((ToolExecution toolExecution) -> {
                ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
            }).onCompleteResponse((ChatResponse response) -> {
                sink.complete();
            }).onError((Throwable error) -> {
                log.error("处理TokenStream流时出错：{}", error.getMessage());
                sink.error(error);
            }).start();
        });

    }


    /**
     * 流式生成并保存HTML代码
     *
     * @param userMessage 用户提示词
     * @return 生成的代码流
     */
//    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
//
//        Flux<String> stringFluxResult = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
//
//
//        StringBuilder codeBuilder = new StringBuilder();
//        return stringFluxResult
//                .doOnNext(
//                        chunk -> {
//                            //在这里可以处理每个代码块的逻辑，例如拼接完整内容
//                            codeBuilder.append(chunk);
//                        }
//                )
//                .doOnComplete(
//                        //在流式传输完成后执行保存操作
//                        () -> {
//                            try {
//                                String completeHtmlCode = codeBuilder.toString();
//                                HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode); //通过解析器获取完整代码结果对象
//                                File savedDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);//保存文件
//                                log.info("HTML代码已保存到目录：{}", savedDir.getAbsolutePath());
//                            } catch (Exception e) {
//                                log.error("保存失败：{}", e.getMessage());
//                            }
//                        }
//                );
//
//
//    }


    /**
     * 生成并保存多文件代码
     *
     * @param userMessage 用户提示词
     * @return 保存的目录对象
     */
//    private File generateAndSaveMultiFileCode(String userMessage) {
//
//        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
//
//        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
//
//
//    }

    /**
     * 生成并保存HTML代码
     *
     * @param userMessage 用户提示词
     * @return 保存的目录对象
     */
//    private File generateAndSaveHtmlCode(String userMessage) {
//
//        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
//        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
//
//
//    }


}
