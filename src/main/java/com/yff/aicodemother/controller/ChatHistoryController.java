package com.yff.aicodemother.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yff.aicodemother.annotation.AuthCheck;
import com.yff.aicodemother.common.BaseResponse;
import com.yff.aicodemother.common.ResultUtils;
import com.yff.aicodemother.common.login.UserHolder;
import com.yff.aicodemother.exception.ErrorCode;
import com.yff.aicodemother.exception.ThrowUtils;
import com.yff.aicodemother.model.dto.chathistory.ChatHistoryAdminQueryRequest;
import com.yff.aicodemother.model.dto.chathistory.ChatHistoryCursorQueryRequest;
import com.yff.aicodemother.model.vo.ChatHistoryVo;
import com.yff.aicodemother.model.vo.CursorPageVo;
import com.yff.aicodemother.service.ChatHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 对话历史 控制层。
 *
 * @author yff
 * @since 2026-02-08
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Autowired
    private ChatHistoryService chatHistoryService;

    // ==================== 用户接口 ====================

    /**
     * 分页查询指定应用的对话历史 (使用游标)
     *
     * @param chatHistoryCursorQueryRequest 查询条件
     * @return 游标分页对话历史 VO 列表
     */
    @PostMapping("/list/page")
    @Operation(summary = "分页查询指定应用的对话历史")
    public BaseResponse<CursorPageVo<ChatHistoryVo>> listChatHistoryByPage(
            @RequestBody ChatHistoryCursorQueryRequest chatHistoryCursorQueryRequest) {
        ThrowUtils.throwIf(chatHistoryCursorQueryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(chatHistoryCursorQueryRequest.getAppId() == null, ErrorCode.PARAMS_ERROR, "应用ID不能为空");

        Long userId = UserHolder.getUserId();
        CursorPageVo<ChatHistoryVo> chatHistoryVoPageByCursor = chatHistoryService.listChatHistoryVoByPage(
                chatHistoryCursorQueryRequest, userId);
        return ResultUtils.success(chatHistoryVoPageByCursor);
    }

    // ==================== 管理员接口 ====================

    /**
     * 【管理员】分页查询所有对话历史
     *
     * @param chatHistoryAdminQueryRequest 查询条件
     * @return 分页对话历史 VO 列表
     */
    @PostMapping("/admin/list/page")
    @Operation(summary = "【管理员】分页查询所有对话历史")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Page<ChatHistoryVo>> adminListChatHistoryByPage(
            @RequestBody ChatHistoryAdminQueryRequest chatHistoryAdminQueryRequest) {
        ThrowUtils.throwIf(chatHistoryAdminQueryRequest == null, ErrorCode.PARAMS_ERROR);

        Page<ChatHistoryVo> chatHistoryVoPage = chatHistoryService.adminListChatHistoryVoByPage(
                chatHistoryAdminQueryRequest);
        return ResultUtils.success(chatHistoryVoPage);
    }

}
