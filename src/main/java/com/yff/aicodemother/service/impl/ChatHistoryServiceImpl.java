package com.yff.aicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
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
import com.yff.aicodemother.model.enums.MessageTypeEnum;
import com.yff.aicodemother.model.vo.ChatHistoryVo;
import com.yff.aicodemother.model.vo.CursorInfo;
import com.yff.aicodemother.model.vo.CursorPageVo;
import com.yff.aicodemother.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {

        try {
            LambdaQueryWrapper<ChatHistory> chatHistoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
            // 先根据appId查询对话历史
            chatHistoryLambdaQueryWrapper.eq(ChatHistory::getAppId, appId)
                    .orderByAsc(ChatHistory::getCreateTime)
                    .last("limit 1," + maxCount); //起始点为1而不是0，跳过第一条，避免重复加载最新消息
            List<ChatHistory> historyList = this.list(chatHistoryLambdaQueryWrapper);
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }

            //反转列表，确保按照时间正序加载到记忆中   老的在前，新的在后
//            historyList = historyList.reversed();
            //按照时间顺序添加到记忆中
            int loadedCount = 0;
            //先清理历史缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistory history : historyList) {
                if (MessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    loadedCount++;
                } else if (MessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    loadedCount++;
                }
            }

            log.info("加载对话历史到记忆中，appId={}，加载数量={}", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载对话历史到记忆中失败，appId={}，错误信息={}", appId, e.getMessage());
            //加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
    }

}
