package com.yff.aicodemother.ai.routing;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * AI代码生成类型智能路由服务工厂类
 *
 *
 * @author yff
 * @date 2026-02-20 15:47:56
 */
@Slf4j
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {

    @Autowired
    private ChatModel chatModel;

    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService() {
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }

}
