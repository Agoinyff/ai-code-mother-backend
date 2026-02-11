package com.yff.aicodemother.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yff.aicodemother.ai.model.enums.CodeGenTypeEnum;
import com.yff.aicodemother.ai.tools.FileWriteTool;
import com.yff.aicodemother.exception.BusinessException;
import com.yff.aicodemother.exception.ErrorCode;
import com.yff.aicodemother.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
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

    @Autowired
    private FileWriteTool fileWriteTool;
    /**
     * AI服务实例缓存
     * 缓存策略:
     * - 最大容量:1000
     * - 写入后30分钟过期
     * - 访问后10分钟过期
     */
    private final Cache<String, AICodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener(((key, value, cause) -> {
                log.debug("AI服务实例被移除,appId:{},原因:{}", key, cause);
            }))
            .build();

    // region 根据appId获取不同的AI服务实例(不带缓存)
    // 为了保证和之前的代码兼容，仍然默认提供一个AICodeGeneratorService实例
    @Bean
    public AICodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorServiceByAppId(0L);
    }

    /*
     * 根据appId获取服务
     */
    public AICodeGeneratorService getAiCodeGeneratorServiceByAppId(Long appId) {
        // 这里可以根据appId返回不同的服务实例

        // 根据appId构建独立的对话记忆
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
    // endregion

    /*
     * 根据appId获取服务 (带缓存) 保存兼容历史逻辑
     */
    // public AICodeGeneratorService getAiCodeGeneratorService(long appId) {
    // return serviceCache.get(appId,this::createAiCodeGeneratorService);//Caffeine
    // 的 Load-Through 模式，开发者无需手动判断缓存是否存在。
    // }

    /*
     * 根据appId和代码生成类型获取服务 (带缓存)
     */
    public AICodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenTypeEnum) {
        String cacheKey = buildCacheKey(appId, codeGenTypeEnum);
        return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenTypeEnum));// Caffeine 的
                                                                                                       // Load-Through
                                                                                                       // 模式，开发者无需手动判断缓存是否存在。
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenTypeEnum) {
        return appId + "_" + codeGenTypeEnum.getValue();
    }

    /**
     * 创建新的AI服务实例
     *
     */
    private AICodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenTypeEnum) {

        log.info("创建新的AI服务实例,appId:{}", appId);

        // 根据appId构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(50)
                .build();
        // 从数据库加载历史对话到记忆中
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 50);

        // 修改代码实现根据codeGenTypeEnum选择不同的配置
        return switch (codeGenTypeEnum) {
            // vue项目 - 使用chatModel而非streamingChatModel，因为Gemini的OpenAI兼容API
            // 在流式模式下返回tool call时，toolCall.index()为null，导致langchain4j的
            // ConcurrentHashMap.computeIfAbsent抛出NullPointerException。
            // 同时配置chatModel和streamingChatModel：tool
            // calling阶段用chatModel（非流式），文本生成用streamingChatModel（流式）
            case VUE_PROJECT -> AiServices.builder(AICodeGeneratorService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(streamingChatModel)
                    .chatMemoryProvider(memoryId -> chatMemory) // 这里必须要指定chatMemoryProvider的配置，为每个memoryId绑定会话记忆
                    .tools(new FileWriteTool())
                    .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage
                            .from(toolExecutionRequest, "Error:there is no tool called " + toolExecutionRequest.name()))
                    .build();
            case HTML, MULTI_FILE -> AiServices.builder(AICodeGeneratorService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(streamingChatModel)
                    .chatMemory(chatMemory)
                    .build();
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型：" + codeGenTypeEnum.getValue());
        };
    }

}
