package com.yff.aicodemother.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yff.aicodemother.model.dto.chathistory.ChatHistoryAdminQueryRequest;
import com.yff.aicodemother.model.dto.chathistory.ChatHistoryCursorQueryRequest;
import com.yff.aicodemother.model.entity.ChatHistory;
import com.yff.aicodemother.model.vo.ChatHistoryVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 对话历史 映射层。
 *
 * @author yff
 * @since 2026-02-08
 */
@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {

    /**
     * 游标分页查询指定应用的对话历史
     *
     * @param query  查询条件（包含游标信息）
     * @param userId 用户 ID（用于权限校验）
     * @param limit  查询条数（实际查询 N+1 条）
     * @return 对话历史 VO 列表
     */
    List<ChatHistoryVo> selectChatHistoryVoByCursor(
            @Param("query") ChatHistoryCursorQueryRequest query,
            @Param("userId") Long userId,
            @Param("limit") int limit);

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
