package com.yff.aicodemother.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yff.aicodemother.annotation.AuthCheck;
import com.yff.aicodemother.common.BaseResponse;
import com.yff.aicodemother.common.ResultUtils;
import com.yff.aicodemother.common.login.UserHolder;
import com.yff.aicodemother.exception.ErrorCode;
import com.yff.aicodemother.exception.ThrowUtils;
import com.yff.aicodemother.model.dto.app.AppAdminQueryRequest;
import com.yff.aicodemother.model.dto.app.AppAdminUpdateRequest;
import com.yff.aicodemother.model.dto.app.AppAddRequest;
import com.yff.aicodemother.model.dto.app.AppQueryRequest;
import com.yff.aicodemother.model.dto.app.AppUpdateRequest;
import com.yff.aicodemother.model.entity.App;
import com.yff.aicodemother.model.vo.AppVo;
import com.yff.aicodemother.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 应用 控制层。
 *
 * @author yff
 * @since 2026-02-06
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Autowired
    private AppService appService;

    // ==================== 普通用户接口 ====================

    /**
     * 创建应用
     *
     * @param appAddRequest 创建应用请求体
     * @return 新应用ID
     */
    @PostMapping("/add")
    @Operation(summary = "创建应用")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        Long userId = UserHolder.getUserId();
        Long appId = appService.createApp(appAddRequest, userId);
        return ResultUtils.success(appId);
    }

    /**
     * 更新自己的应用（仅支持修改名称）
     *
     * @param appUpdateRequest 更新应用请求体
     * @return 是否更新成功
     */
    @PostMapping("/update/my")
    @Operation(summary = "更新自己的应用")
    public BaseResponse<Boolean> updateMyApp(@RequestBody AppUpdateRequest appUpdateRequest) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        Long userId = UserHolder.getUserId();
        Boolean result = appService.updateMyApp(appUpdateRequest, userId);
        return ResultUtils.success(result);
    }

    /**
     * 删除自己的应用
     *
     * @param id 应用ID
     * @return 是否删除成功
     */
    @PostMapping("/delete/my")
    @Operation(summary = "删除自己的应用")
    public BaseResponse<Boolean> deleteMyApp(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        Long userId = UserHolder.getUserId();
        Boolean result = appService.deleteMyApp(id, userId);
        return ResultUtils.success(result);
    }

    /**
     * 根据 ID 获取应用详情（脱敏）
     *
     * @param id 应用ID
     * @return 应用VO
     */
    @GetMapping("/get/vo")
    @Operation(summary = "根据 ID 获取应用详情")
    public BaseResponse<AppVo> getAppVoById(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        AppVo appVo = appService.getAppVoById(id);
        ThrowUtils.throwIf(appVo == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        return ResultUtils.success(appVo);
    }

    /**
     * 分页查询我的应用列表
     *
     * @param appQueryRequest 查询条件
     * @return 分页应用VO列表
     */
    @PostMapping("/list/my/page/vo")
    @Operation(summary = "分页查询我的应用列表")
    public BaseResponse<Page<AppVo>> listMyAppVoByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long userId = UserHolder.getUserId();
        Page<AppVo> appVoPage = appService.listMyAppVoByPage(appQueryRequest, userId);
        return ResultUtils.success(appVoPage);
    }

    /**
     * 分页查询精选应用列表
     *
     * @param appQueryRequest 查询条件
     * @return 分页应用VO列表
     */
    @PostMapping("/list/featured/page/vo")
    @Operation(summary = "分页查询精选应用列表")
    public BaseResponse<Page<AppVo>> listFeaturedAppVoByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<AppVo> appVoPage = appService.listFeaturedAppVoByPage(appQueryRequest);
        return ResultUtils.success(appVoPage);
    }

    // ==================== 管理员接口 ====================

    /**
     * 【管理员】根据 ID 删除任意应用
     *
     * @param id 应用ID
     * @return 是否删除成功
     */
    @PostMapping("/admin/delete")
    @Operation(summary = "【管理员】根据 ID 删除任意应用")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> adminDeleteApp(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        Boolean result = appService.adminDeleteApp(id);
        return ResultUtils.success(result);
    }

    /**
     * 【管理员】更新任意应用
     *
     * @param appAdminUpdateRequest 更新应用请求体
     * @return 是否更新成功
     */
    @PostMapping("/admin/update")
    @Operation(summary = "【管理员】更新任意应用")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> adminUpdateApp(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null,
                ErrorCode.PARAMS_ERROR);
        Boolean result = appService.adminUpdateApp(appAdminUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 【管理员】分页查询应用列表
     *
     * @param appAdminQueryRequest 查询条件
     * @return 分页应用VO列表
     */
    @PostMapping("/admin/list/page/vo")
    @Operation(summary = "【管理员】分页查询应用列表")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Page<AppVo>> adminListAppVoByPage(@RequestBody AppAdminQueryRequest appAdminQueryRequest) {
        ThrowUtils.throwIf(appAdminQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<AppVo> appVoPage = appService.adminListAppVoByPage(appAdminQueryRequest);
        return ResultUtils.success(appVoPage);
    }

    /**
     * 【管理员】根据 ID 获取应用详情（未脱敏）
     *
     * @param id 应用ID
     * @return 应用完整信息
     */
    @GetMapping("/admin/get")
    @Operation(summary = "【管理员】根据 ID 获取应用详情（未脱敏）")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<App> adminGetAppById(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        return ResultUtils.success(app);
    }

}
