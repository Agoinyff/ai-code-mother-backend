package com.yff.aicodemother.ai.core;


import com.yff.aicodemother.ai.model.HtmlCodeResult;
import com.yff.aicodemother.ai.model.MultiFileCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码解析器
 * 提供静态方法解析不同类型的代码内容，，，使用正则匹配的方式解析代码片段
 *
 * @author yff
 * @date 2026-02-05 16:04:50
 */
public class CodeParser {

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 解析HTML代码内容，提取HTML代码片段
     *
     * @param codeContent 包含HTML代码的内容
     * @return 提取的HTML代码结果对象
     */
    public static HtmlCodeResult parseHtmlCode(String codeContent) {

        HtmlCodeResult htmlCodeResult = new HtmlCodeResult();
        //提取HTML代码
        String htmlCode = extractHtmlCode(codeContent);

        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            htmlCodeResult.setHtmlCode(htmlCode.trim());
        } else {
            //如果没有找到代码块，将整个内容作为HTML代码返回
            htmlCodeResult.setHtmlCode(codeContent.trim());
        }
        return htmlCodeResult;
    }


    /**
     * 解析多文件代码内容，提取HTML、CSS和JS代码片段
     *
     * @param codeContent 包含多文件代码的内容
     * @return 提取的多文件代码结果对象
     */
    public static MultiFileCodeResult parseMultiFileCode(String codeContent) {
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
     * 提取HTML代码片段
     *
     * @param codeContent 包含HTML代码的内容
     * @return 提取的HTML代码片段，如果未找到则返回null
     */
    private static String extractHtmlCode(String codeContent) {

        Matcher matcher = HTML_CODE_PATTERN.matcher(codeContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;

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
