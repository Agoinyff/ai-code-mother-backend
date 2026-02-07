package com.yff.aicodemother.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yff.aicodemother.model.dto.app.AppAdminQueryRequest;
import com.yff.aicodemother.model.dto.app.AppAdminUpdateRequest;
import com.yff.aicodemother.model.dto.app.AppAddRequest;
import com.yff.aicodemother.model.dto.app.AppQueryRequest;
import com.yff.aicodemother.model.dto.app.AppUpdateRequest;
import com.yff.aicodemother.model.entity.App;
import com.yff.aicodemother.model.entity.User;
import com.yff.aicodemother.model.vo.AppVo;
import reactor.core.publisher.Flux;

/**
 * 应用 服务层。
 *
 * @author yff
 * @since 2026-02-06
 */
public interface AppService extends IService<App> {

    /**
     * 创建应用
     *
     * @param appAddRequest 创建应用请求
     * @param userId        创建用户ID
     * @return 新应用ID
     */
    Long createApp(AppAddRequest appAddRequest, Long userId);

    /**
     * 用户更新自己的应用
     *
     * @param appUpdateRequest 更新请求
     * @param userId           当前用户ID
     * @return 是否更新成功
     */
    Boolean updateMyApp(AppUpdateRequest appUpdateRequest, Long userId);

    /**
     * 用户删除自己的应用
     *
     * @param appId  应用ID
     * @param userId 当前用户ID
     * @return 是否删除成功
     */
    Boolean deleteMyApp(Long appId, Long userId);

    /**
     * 根据ID获取应用VO（脱敏）
     *
     * @param appId 应用ID
     * @return 应用VO
     */
    AppVo getAppVoById(Long appId);

    /**
     * 分页查询我的应用列表
     *
     * @param appQueryRequest 查询条件
     * @param userId          当前用户ID
     * @return 分页应用VO列表
     */
    Page<AppVo> listMyAppVoByPage(AppQueryRequest appQueryRequest, Long userId);

    /**
     * 分页查询精选应用列表
     *
     * @param appQueryRequest 查询条件
     * @return 分页应用VO列表
     */
    Page<AppVo> listFeaturedAppVoByPage(AppQueryRequest appQueryRequest);

    /**
     * 【管理员】删除任意应用
     *
     * @param appId 应用ID
     * @return 是否删除成功
     */
    Boolean adminDeleteApp(Long appId);

    /**
     * 【管理员】更新任意应用
     *
     * @param appAdminUpdateRequest 更新请求
     * @return 是否更新成功
     */
    Boolean adminUpdateApp(AppAdminUpdateRequest appAdminUpdateRequest);

    /**
     * 【管理员】分页查询应用列表
     *
     * @param appAdminQueryRequest 查询条件
     * @return 分页应用VO列表
     */
    Page<AppVo> adminListAppVoByPage(AppAdminQueryRequest appAdminQueryRequest);

    /**
     * 流式对话生成代码
     *
     * @param appId       应用ID
     * @param userMessage 用户消息
     * @param user        用户信息
     * @return 生成的代码流
     */
    Flux<String> chatToGenCode(Long appId, String userMessage, User user);

}
