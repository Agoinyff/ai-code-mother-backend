package com.yff.aicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yff.aicodemother.exception.BusinessException;
import com.yff.aicodemother.exception.ErrorCode;
import com.yff.aicodemother.mapper.AppMapper;
import com.yff.aicodemother.mapper.ChatHistoryMapper;
import com.yff.aicodemother.model.dto.chathistory.ChatHistoryAdminQueryRequest;
import com.yff.aicodemother.model.dto.chathistory.ChatHistoryAddRequest;
import com.yff.aicodemother.model.dto.chathistory.ChatHistoryCursorQueryRequest;
import com.yff.aicodemother.model.entity.App;
import com.yff.aicodemother.model.entity.ChatHistory;
import com.yff.aicodemother.model.vo.ChatHistoryVo;
import com.yff.aicodemother.model.vo.CursorInfo;
import com.yff.aicodemother.model.vo.CursorPageVo;
import com.yff.aicodemother.service.ChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author yff
 * @since 2026-02-08
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>
        implements ChatHistoryService {

    @Autowired
    private ChatHistoryMapper chatHistoryMapper;

    @Autowired
    private AppMapper appMapper;

    @Override
    public Long saveChatMessage(ChatHistoryAddRequest request) {
        // 参数校验
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        if (request.getAppId() == null || request.getAppId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID不合法");
        }
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        if (request.getMessage() == null || request.getMessage().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        }
        if (request.getMessageType() == null || request.getMessageType().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        }

        // 创建对话历史记录
        ChatHistory chatHistory = BeanUtil.copyProperties(request, ChatHistory.class);
        boolean result = this.save(chatHistory);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存对话消息失败");
        }
        return chatHistory.getId();
    }

    @Override
    public CursorPageVo<ChatHistoryVo> listChatHistoryVoByPage(ChatHistoryCursorQueryRequest cursorRequest, Long userId) {
        // 参数校验
        if (cursorRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        if (cursorRequest.getAppId() == null || cursorRequest.getAppId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID不合法");
        }
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }

        // 权限校验：用户只能查询自己创建的应用的对话历史
        App app = appMapper.selectById(cursorRequest.getAppId());
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        if (!app.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        }

        // 设置默认分页大小
        Integer pageSize = cursorRequest.getPageSize();
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }
        // 限制最大查询条数
        if (pageSize > 100) {
            pageSize = 100;
        }

        // 查询 N+1 条数据，用于判断是否还有下一页
        int queryLimit = pageSize + 1;
        List<ChatHistoryVo> records = chatHistoryMapper.selectChatHistoryVoByCursor(
                cursorRequest, userId, queryLimit);

        // 判断是否还有下一页
        boolean hasMore = records.size() > pageSize;
        
        // 如果查询结果超过 pageSize，移除最后一条（第 N+1 条）
        if (hasMore) {
            records = records.subList(0, pageSize);
        }

        // 构建下一页的游标信息
        CursorInfo nextCursor = null;
        if (hasMore && !records.isEmpty()) {
            ChatHistoryVo lastRecord = records.get(records.size() - 1);
            nextCursor = CursorInfo.builder()
                    .lastTime(lastRecord.getCreateTime())
                    .lastId(lastRecord.getId())
                    .build();
        }

        // 构建返回结果
        return CursorPageVo.<ChatHistoryVo>builder()
                .records(records)
                .hasMore(hasMore)
                .nextCursor(nextCursor)
                .build();
    }

    @Override
    public Page<ChatHistoryVo> adminListChatHistoryVoByPage(ChatHistoryAdminQueryRequest request) {
        // 参数校验
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }

        // 分页查询（管理员可查询所有对话历史）
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        IPage<ChatHistoryVo> chatHistoryVoPage = chatHistoryMapper.selectChatHistoryVoPageForAdmin(
                new Page<>(pageNum, pageSize), request);
        return (Page<ChatHistoryVo>) chatHistoryVoPage;
    }

    @Override
    public Boolean deleteByAppId(Long appId) {
        // 参数校验
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID不合法");
        }

        // 逻辑删除该应用的所有对话历史
        LambdaUpdateWrapper<ChatHistory> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatHistory::getAppId, appId)
                .eq(ChatHistory::getIsDelete, 0)
                .set(ChatHistory::getIsDelete, 1);
        return this.update(updateWrapper);
    }

}
