package com.yff.aicodemother.ai.core;


import com.yff.aicodemother.ai.AICodeGeneratorService;
import com.yff.aicodemother.ai.model.HtmlCodeResult;
import com.yff.aicodemother.ai.model.MultiFileCodeResult;
import com.yff.aicodemother.ai.model.enums.CodeGenTypeEnum;
import com.yff.aicodemother.exception.BusinessException;
import com.yff.aicodemother.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * @author yff
 * @date 2026-02-05 14:21:58
 * <p>
 * AI生成代码门面类，组合生成和保存功能，提供统一的代码生成接口
 */
@Service
public class AiCodeGeneratorFacade {


    @Autowired
    private AICodeGeneratorService aiCodeGeneratorService;


    /**
     * 统一入口：根据类型生成并保存代码文件
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 代码生成类型枚举
     * @return 保存的目录对象
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {

        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }

        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            default -> {
                String errorMsg = String.format("不支持的生成类型：%s", codeGenTypeEnum.getValue());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMsg);
            }
        };


    }

    /**
     * 生成并保存多文件代码
     *
     * @param userMessage 用户提示词
     * @return 保存的目录对象
     */
    private File generateAndSaveMultiFileCode(String userMessage) {

        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);

        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);


    }

    /**
     * 生成并保存HTML代码
     *
     * @param userMessage 用户提示词
     * @return 保存的目录对象
     */
    private File generateAndSaveHtmlCode(String userMessage) {

        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);


    }


}
