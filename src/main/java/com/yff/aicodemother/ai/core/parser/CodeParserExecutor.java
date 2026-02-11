package com.yff.aicodemother.ai.core.parser;


import com.yff.aicodemother.ai.model.enums.CodeGenTypeEnum;
import com.yff.aicodemother.exception.BusinessException;
import com.yff.aicodemother.exception.ErrorCode;

/**
 * 代码解析器执行器
 * 根据代码生成类型执行相应的解析逻辑
 *
 * @author yff
 * @date 2026-02-06 09:29:41
 */
public class CodeParserExecutor {


    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();

    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();


    /**
     * 执行代码解析
     *
     * @param codeContent 代码内容
     * @param codeGenType 代码生成类型（如 "HTML_SINGLE_FILE", "MULTI_FILE"）
     * @return 解析结果对象
     */
    public static Object executeParse(String codeContent, CodeGenTypeEnum codeGenType) {

        //TODO 判断用户是对话还是生成代码

        return switch (codeGenType) {
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型" + codeGenType);
        };


    }

}
