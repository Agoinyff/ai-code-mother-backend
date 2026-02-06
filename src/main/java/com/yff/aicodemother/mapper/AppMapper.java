package com.yff.aicodemother.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yff.aicodemother.model.dto.app.AppAdminQueryRequest;
import com.yff.aicodemother.model.dto.app.AppQueryRequest;
import com.yff.aicodemother.model.entity.App;
import com.yff.aicodemother.model.vo.AppVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 应用 映射层。
 *
 * @author yff
 * @since 2026-02-06
 */
@Mapper
public interface AppMapper extends BaseMapper<App> {

    /**
     * 分页查询用户自己的应用列表
     *
     * @param page   分页参数
     * @param query  查询条件
     * @param userId 用户ID
     * @return 分页 VO 结果
     */
    IPage<AppVo> selectMyAppVoPage(Page<AppVo> page, @Param("query") AppQueryRequest query,
            @Param("userId") Long userId);

    /**
     * 分页查询精选应用列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页 VO 结果
     */
    IPage<AppVo> selectFeaturedAppVoPage(Page<AppVo> page, @Param("query") AppQueryRequest query);

    /**
     * 管理员分页查询应用列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页 VO 结果
     */
    IPage<AppVo> selectAppVoPageForAdmin(Page<AppVo> page, @Param("query") AppAdminQueryRequest query);

}
