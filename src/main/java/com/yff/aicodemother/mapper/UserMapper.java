package com.yff.aicodemother.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yff.aicodemother.model.dto.user.UserQueryRequest;
import com.yff.aicodemother.model.entity.User;
import com.yff.aicodemother.model.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 映射层。
 *
 * @author yff
 * @since 2026-02-02 15:36:30
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 分页查询用户列表（直接返回 VO，数据脱敏）
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页 VO 结果
     */
    IPage<UserVo> selectUserVoPage(Page<UserVo> page, @Param("query") UserQueryRequest query);

}
