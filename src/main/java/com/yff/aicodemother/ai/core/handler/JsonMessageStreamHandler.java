package com.yff.aicodemother.ai.core.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yff.aicodemother.ai.core.builder.VueProjectBuilder;
import com.yff.aicodemother.ai.model.message.*;
import com.yff.aicodemother.constant.AppConstant;
import com.yff.aicodemother.model.dto.chathistory.ChatHistoryAddRequest;
import com.yff.aicodemother.model.entity.User;
import com.yff.aicodemother.model.enums.MessageTypeEnum;
import com.yff.aicodemother.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashSet;

/**
 *
 * JSON消息流处理器
 * 处理 VUE_PROJECT 类型的复杂流式响应，包含工具调用信息
 * 开发逻辑：
 * 1.消息解析：需要根据消息类型，将json字符串转换为对应的消息对象，然后提取属性进行其他操作
 * 2.输出选择工具信息：虽然后端实现了工具调用的流式输出，但是考虑到前端不好对这些消息进行解析和处理，因此我们只在同一个工具第一次输出时
 * ，输出给前端“选择工具”的消息。可以利用一个集合来判断某个id的工具是否是首次输出
 *
 * @author yff
 * @date 2026-02-12 10:41:53
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {

    @Autowired
    private VueProjectBuilder vueProjectBuilder;

    /**
     * * 处理 TokenStream 中的 JSON 消息流，解析消息并提取工具调用信息，同时构建后端记忆格式的聊天历史字符串
     *
     * @param originalFlux       原始流
     * @param chatHistoryService 聊天记录服务
     * @param appId              应用ID
     * @param loginUser          登录用户信息
     * @return 处理后的消息流
     */
    public Flux<String> handle(Flux<String> originalFlux,
            ChatHistoryService chatHistoryService,
            long appId, User loginUser) {
        // 收集数据用于生成后端记忆格式
        StringBuilder chatHistoryStringBuilder = new StringBuilder();

        // 用于跟踪已经见过的工具ID,判断是否是第一次调用
        HashSet<String> seenToolIds = new HashSet<>();

        return originalFlux
                .map(chunk -> {
                    // 解析每一个JSON消息块
                    return handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenToolIds);
                })
                .filter(StrUtil::isNotEmpty)// 过滤空字符串
                .doOnComplete(() -> {
                    // 流处理完成后，保存聊天记录到数据库
                    ChatHistoryAddRequest aiResponse = new ChatHistoryAddRequest();
                    aiResponse.setAppId(appId);
                    aiResponse.setUserId(loginUser.getId());
                    aiResponse.setMessage(chatHistoryStringBuilder.toString());
                    aiResponse.setMessageType(MessageTypeEnum.AI.getValue());
                    chatHistoryService.saveChatMessage(aiResponse);
                    //异步构建Vue项目
                    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project" + appId;
                    vueProjectBuilder.buildProjectAsync(projectPath);
                }).doOnError(error -> {
                    // 流出错时，保存错误消息到对话历史
                    ChatHistoryAddRequest errorHistoryRequest = new ChatHistoryAddRequest();
                    errorHistoryRequest.setAppId(appId);
                    errorHistoryRequest.setUserId(loginUser.getId());
                    errorHistoryRequest.setMessage("AI 回复失败：" + error.getMessage());
                    errorHistoryRequest.setMessageType(MessageTypeEnum.AI.getValue());
                    chatHistoryService.saveChatMessage(errorHistoryRequest);
                });
    }

    /**
     * 处理单个JSON消息块 解析并收集TokenStream数据
     * 
     * @param chunk                    JSON消息块
     * @param chatHistoryStringBuilder 用于构建聊天历史的字符串构建器
     * @param seenToolIds              已经见过的工具ID集合
     * @return 处理后的字符串
     */
    private String handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder,
            HashSet<String> seenToolIds) {

        // 验证chunk是否为有效的JSON对象
        if (StrUtil.isBlank(chunk) || !chunk.trim().startsWith("{")) {
            log.debug("跳过非JSON格式的chunk: {}", chunk);
            return "";
        }

        // 尝试解析JSON，如果解析失败则跳过
        StreamMessage streamMessage;
        try {
            streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        } catch (Exception e) {
            log.warn("无法解析JSON chunk，跳过: {}, 错误: {}", chunk, e.getMessage());
            return "";
        }

        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());

        // 如果类型为null或不支持，跳过该消息
        if (typeEnum == null) {
            log.warn("未知的消息类型: {}, 跳过处理", streamMessage.getType());
            return "";
        }
        switch (typeEnum) {
            case AI_RESPONSER -> {
                AiResponseMessage aiResponseMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                String data = aiResponseMessage.getData();
                // 直接拼接响应
                chatHistoryStringBuilder.append(data);
                return data;
            }
            case TOOL_REQUEST -> {
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                String toolId = toolRequestMessage.getId();
                // 检查是否是第一次出现该工具调用
                if (toolId != null && !seenToolIds.contains(toolId)) {
                    // 如果是第一次调用，则记录ID并完整返回工具信息
                    seenToolIds.add(toolId);
                    return "\n\n[选择工具] 写入文件\n\n";

                } else {
                    return "";
                }

            }
            case TOOL_EXECUTED -> {
                ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                String relativePath = jsonObject.getStr("relativePath");
                String suffix = FileUtil.getSuffix(relativePath);
                String content = jsonObject.getStr("content");
                String result = String.format("""
                        [工具调用] 写入文件：%s
                        ```%s
                        %s
                        ```
                        """, relativePath, suffix, content);
                // 输出给前端和要持久化的内容
                String output = String.format("\n\n%s\n\n", result);
                chatHistoryStringBuilder.append(output);
                return output;
            }
            default -> {
                log.error("不支持的消息类型：{}", typeEnum);
                return "";
            }
        }

    }

}
