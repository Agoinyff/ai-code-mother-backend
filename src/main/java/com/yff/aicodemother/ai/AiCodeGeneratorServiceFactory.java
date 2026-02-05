package com.yff.aicodemother.ai;


import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yff
 * @date 2026-02-05 10:38:17
 */
@Configuration
public class AiCodeGeneratorServiceFactory {


    @Autowired
    private ChatModel chatModel;

    @Bean
    public AICodeGeneratorService aiCodeGeneratorService(){
        return AiServices.create(AICodeGeneratorService.class,chatModel);
    }


}
