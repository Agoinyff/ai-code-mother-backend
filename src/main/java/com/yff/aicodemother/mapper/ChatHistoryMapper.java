package com.yff.aicodemother.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yff.aicodemother.model.dto.chathistory.ChatHistoryAdminQueryRequest;
import com.yff.aicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.yff.aicodemother.model.entity.ChatHistory;
import com.yff.aicodemother.model.vo.ChatHistoryVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 对话历史 映射层。
 *
 * @author yff
 * @since 2026-02-08
 */
@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {

    /**
     * 分页查询指定应用的对话历史
     *
     * @param page   分页参数
     * @param query  查询条件
     * @param userId 用户 ID（用于权限校验）
     * @return 分页 VO 结果
     */
    IPage<ChatHistoryVo> selectChatHistoryVoPage(Page<ChatHistoryVo> page,
            @Param("query") ChatHistoryQueryRequest query,
            @Param("userId") Long userId);

    /**
     * 管理员分页查询所有对话历史
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页 VO 结果
     */
    IPage<ChatHistoryVo> selectChatHistoryVoPageForAdmin(Page<ChatHistoryVo> page,
            @Param("query") ChatHistoryAdminQueryRequest query);

}
