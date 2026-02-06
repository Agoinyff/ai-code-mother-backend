package com.yff.aicodemother.ai.core.parser;


import com.yff.aicodemother.ai.model.MultiFileCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * 多文件代码解析器
 *
 * @author yff
 * @date 2026-02-06 09:27:13
 */
public class MultiFileCodeParser implements CodeParser<MultiFileCodeResult>{

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);



    /**
     * 解析多文件代码内容，提取HTML、CSS和JS代码片段
     *
     * @param codeContent 包含多文件代码的内容
     * @return 提取的多文件代码结果对象
     */
    @Override
    public MultiFileCodeResult parseCode(String codeContent) {
        MultiFileCodeResult multiFileCodeResult = new MultiFileCodeResult();

        //提取HTML代码
        String htmlCode = extractCodeByPattern(codeContent, HTML_CODE_PATTERN);
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            multiFileCodeResult.setHtmlCode(htmlCode.trim());
        }

        //提取CSS代码
        String cssCode = extractCodeByPattern(codeContent, CSS_CODE_PATTERN);
        if (cssCode != null && !cssCode.trim().isEmpty()) {
            multiFileCodeResult.setCssCode(cssCode.trim());
        }

        //提取JS代码
        String jsCode = extractCodeByPattern(codeContent, JS_CODE_PATTERN);
        if (jsCode != null && !jsCode.trim().isEmpty()) {
            multiFileCodeResult.setJsCode(jsCode.trim());
        }

        return multiFileCodeResult;
    }


    /**
     * 根据正则模式提取代码片段
     *
     * @param codeContent 原始内容
     * @param pattern     用于匹配代码片段的正则模式
     * @return 提取的代码片段，如果未找到则返回null
     */
    private static String extractCodeByPattern(String codeContent, Pattern pattern) {
        Matcher matcher = pattern.matcher(codeContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;

    }
}
