package com.yff.aicodemother.ai.routing;

import com.yff.aicodemother.ai.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * AI代码生成类型智能路由服务
 * 使用结构化输出直接返回枚举类型
 *
 *
 * @author yff
 * @date 2026-02-20 15:38:24
 */
public interface AiCodeGenTypeRoutingService {

    /**
     * 根据用户输入的提示，智能路由到具体的代码生成类型
     * 
     * @param userPrompt 用户输入的提示信息
     * @return 代码生成类型枚举值，表示应该使用哪种代码生成方式来处理用户的请求
     */

    @SystemMessage(fromResource = "prompt/codegen-routing-system-prompt.txt")
    CodeGenTypeEnum routeCodeGenType(String userPrompt);

}
