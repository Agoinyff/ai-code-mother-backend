package com.yff.aicodemother.ai.core.handler;


import com.yff.aicodemother.ai.model.enums.CodeGenTypeEnum;
import com.yff.aicodemother.model.entity.User;
import com.yff.aicodemother.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 *
 * 流处理器执行器
 * 根据代码生成类型创建合适的流处理器
 * 1.传统的Flux<String>流(HTML,MULTI_FILE等) -> SimpleStreamHandler
 * 2.TokenStream复杂的流(VUE_PROJECT) -> JsonMessageStreamHandler
 *
 * @author yff
 * @date 2026-02-12 11:26:32
 */
@Slf4j
@Component
public class StreamHandlerExecutor {


    @Autowired
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    @Autowired
    private SimpleTextStreamHandler simpleTextStreamHandler;


    /**
     *
     * 创建流处理器并处理聊天历史记录
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @param codeGenTypeEnum    代码生成类型枚举
     * @return 处理后的流
     */
    public Flux<String> doExecute(Flux<String> originFlux,
                                  ChatHistoryService chatHistoryService,
                                  long appId, User loginUser, CodeGenTypeEnum codeGenTypeEnum) {


        return switch (codeGenTypeEnum) {
            case VUE_PROJECT -> jsonMessageStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser);
            case HTML, MULTI_FILE -> simpleTextStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser);
        };


    }


}
