package com.yff.aicodemother.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yff.aicodemother.model.dto.chathistory.ChatHistoryAdminQueryRequest;
import com.yff.aicodemother.model.dto.chathistory.ChatHistoryAddRequest;
import com.yff.aicodemother.model.dto.chathistory.ChatHistoryCursorQueryRequest;
import com.yff.aicodemother.model.entity.ChatHistory;
import com.yff.aicodemother.model.vo.ChatHistoryVo;
import com.yff.aicodemother.model.vo.CursorPageVo;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

/**
 * 对话历史 服务层。
 *
 * @author yff
 * @since 2026-02-08
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 保存对话消息
     *
     * @param request 保存请求
     * @return 新消息 ID
     */
    Long saveChatMessage(ChatHistoryAddRequest request);

    /**
     * 用户分页查询指定应用的对话历史（游标分页）
     *
     * @param request 查询条件
     * @param userId  当前用户 ID（用于权限校验）
     * @return 游标分页 VO 结果
     */
    CursorPageVo<ChatHistoryVo> listChatHistoryVoByPage(ChatHistoryCursorQueryRequest request, Long userId);

    /**
     * 管理员分页查询所有对话历史
     *
     * @param request 查询条件
     * @return 分页 VO 结果
     */
    Page<ChatHistoryVo> adminListChatHistoryVoByPage(ChatHistoryAdminQueryRequest request);

    /**
     * 删除指定应用的所有对话历史（级联删除用）
     *
     * @param appId 应用 ID
     * @return 是否删除成功
     */
    Boolean deleteByAppId(Long appId);


    /**
     * 加载指定应用的对话历史到聊天记忆中（按时间倒序加载）
     *
     * @param appId      应用ID
     * @param chatMemory 聊天记忆对象
     * @param maxCount   最大加载数量
     * @return 实际加载数量
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);


}
