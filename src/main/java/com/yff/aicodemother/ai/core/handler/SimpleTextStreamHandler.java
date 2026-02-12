package com.yff.aicodemother.ai.core.handler;


import com.yff.aicodemother.model.dto.chathistory.ChatHistoryAddRequest;
import com.yff.aicodemother.model.entity.User;
import com.yff.aicodemother.model.enums.MessageTypeEnum;
import com.yff.aicodemother.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 *
 * 简单文本处理器
 * 处理HTML和多文件代码生成的流式响应。。   其实就是把之前的流处理逻辑移动到了单独的类中
 *
 * @author yff
 * @date 2026-02-12 10:18:16
 */
@Slf4j
@Component
public class SimpleTextStreamHandler {


    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User user) {

        // 2. 生成代码流并保存 AI 响应
        StringBuilder aiResponseBuilder = new StringBuilder();

        return originFlux
                .doOnNext(chunk -> {
                    // 累积 AI 响应内容
                    aiResponseBuilder.append(chunk);
                })
                .doOnComplete(() -> {
                    // 流完成时，保存 AI 消息到对话历史
                    ChatHistoryAddRequest aiHistoryRequest = new ChatHistoryAddRequest();
                    aiHistoryRequest.setAppId(appId);
                    aiHistoryRequest.setUserId(user.getId());
                    aiHistoryRequest.setMessage(aiResponseBuilder.toString());
                    aiHistoryRequest.setMessageType(MessageTypeEnum.AI.getValue());
                    chatHistoryService.saveChatMessage(aiHistoryRequest);
                })
                .doOnError(error -> {
                    // 流出错时，保存错误消息到对话历史
                    ChatHistoryAddRequest errorHistoryRequest = new ChatHistoryAddRequest();
                    errorHistoryRequest.setAppId(appId);
                    errorHistoryRequest.setUserId(user.getId());
                    errorHistoryRequest.setMessage("AI 回复失败：" + error.getMessage());
                    errorHistoryRequest.setMessageType(MessageTypeEnum.AI.getValue());
                    chatHistoryService.saveChatMessage(errorHistoryRequest);
                });


    }


}
