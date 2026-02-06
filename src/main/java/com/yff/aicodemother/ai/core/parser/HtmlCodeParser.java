package com.yff.aicodemother.ai.core.parser;


import com.yff.aicodemother.ai.model.HtmlCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * HTML单文件代码解析器
 *
 * @author yff
 * @date 2026-02-06 09:24:59
 */
public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);



    /**
     * 解析HTML代码内容，提取HTML代码片段
     *
     * @param codeContent 包含HTML代码的内容
     * @return 提取的HTML代码结果对象
     */
    @Override
    public HtmlCodeResult parseCode(String codeContent) {
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
}
