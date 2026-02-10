package com.yff.aicodemother.ai;



import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yff.aicodemother.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Function;

/**
 * @author yff
 * @date 2026-02-05 10:38:17
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {


    @Autowired
    private ChatModel chatModel;

    @Autowired
    private StreamingChatModel streamingChatModel;

    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;

    @Autowired
    private ChatHistoryService chatHistoryService;
    /**
     * AI服务实例缓存
     * 缓存策略:
     * - 最大容量:1000
     * - 写入后30分钟过期
     * - 访问后10分钟过期
     */
    private final Cache<Long,AICodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener(((key, value, cause) -> {
                log.debug("AI服务实例被移除,appId:{},原因:{}",key,cause);
            }))
            .build();


    //region 根据appId获取不同的AI服务实例(不带缓存)
    //为了保证和之前的代码兼容，仍然默认提供一个AICodeGeneratorService实例
    @Bean
    public AICodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorServiceByAppId(0L);
    }

    /*
     根据appId获取服务
     */
    public AICodeGeneratorService getAiCodeGeneratorServiceByAppId(Long appId) {
        //这里可以根据appId返回不同的服务实例

        //根据appId构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();



        return AiServices.builder(AICodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();
    }
//endregion


    /*
     根据appId获取服务 (带缓存)
     */
    public AICodeGeneratorService getAiCodeGeneratorService(long appId) {
        return serviceCache.get(appId,this::createAiCodeGeneratorService);//Caffeine 的 Load-Through 模式，开发者无需手动判断缓存是否存在。
    }


    /**
     * 创建新的AI服务实例
     *
     */
    private AICodeGeneratorService createAiCodeGeneratorService(long appId){

        log.info("创建新的AI服务实例,appId:{}",appId);

        //根据appId构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        //从数据库加载历史对话到记忆中
        chatHistoryService.loadChatHistoryToMemory(appId,chatMemory,50);
        return AiServices.builder(AICodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();
    }







}
