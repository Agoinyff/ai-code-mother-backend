package com.yff.aicodemother.ai.core.parser;


/**
 *
 * 代码解析器策略接口
 *
 *
 * @author yff
 * @date 2026-02-06 09:22:11
 */
public interface CodeParser<T> {


    /**
     * 解析代码内容
     *
     * @param codeContent 原始代码内容
     * @return 解析结果
     */
    T parseCode(String codeContent);

}
