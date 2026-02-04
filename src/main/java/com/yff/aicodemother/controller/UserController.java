package com.yff.aicodemother.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yff.aicodemother.annotation.AuthCheck;
import com.yff.aicodemother.common.BaseResponse;
import com.yff.aicodemother.common.ResultUtils;
import com.yff.aicodemother.common.login.UserHolder;
import com.yff.aicodemother.exception.BusinessException;
import com.yff.aicodemother.exception.ErrorCode;
import com.yff.aicodemother.exception.ThrowUtils;
import com.yff.aicodemother.model.dto.user.UserAddRequest;
import com.yff.aicodemother.model.dto.user.UserLoginRequest;
import com.yff.aicodemother.model.dto.user.UserQueryRequest;
import com.yff.aicodemother.model.dto.user.UserRegisterRequest;
import com.yff.aicodemother.model.dto.user.UserUpdateRequest;
import com.yff.aicodemother.model.entity.User;
import com.yff.aicodemother.model.enums.UserRoleEnum;
import com.yff.aicodemother.model.vo.LoginVo;
import com.yff.aicodemother.model.vo.UserVo;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yff.aicodemother.service.UserService;

/**
 * 用户 控制层。
 *
 * @author yff
 * @since 2026-02-02 15:36:30
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // ==================== 基础认证接口 ====================

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求体
     * @return 新用户id
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        Long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求体
     * @return 登录信息
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public BaseResponse<LoginVo> userLogin(@RequestBody UserLoginRequest userLoginRequest) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginVo loginVo = userService.login(userAccount, userPassword);

        // 返回jwt生成的token
        return ResultUtils.success(loginVo);
    }

    /**
     * 用户登出
     *
     * @return 是否登出成功
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public BaseResponse<Boolean> userLogout() {
        Boolean result = userService.logout(UserHolder.getUserId());
        return ResultUtils.success(result);
    }

    /**
     * 获取登录用户信息
     *
     * @return 登录用户信息
     */
    @GetMapping("/get/login")
    @Operation(summary = "获取登录用户信息")
    public BaseResponse<UserVo> getLoginUserInfo() {
        Long userId = UserHolder.getUserId();
        UserVo userVo = userService.getUserVoById(userId);
        return ResultUtils.success(userVo);
    }

    // ==================== 管理员接口 ====================

    /**
     * 【管理员】创建用户
     *
     * @param userAddRequest 创建用户请求体
     * @return 新用户id
     */
    @PostMapping("/add")
    @Operation(summary = "【管理员】创建用户")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);

        User user = BeanUtil.copyProperties(userAddRequest, User.class);
        // 设置默认密码
        user.setUserPassword(userService.getEncryptedPassword("12345678"));
        // 设置默认角色
        if (user.getUserRole() == null) {
            user.setUserRole(UserRoleEnum.USER.getValue());
        }
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "创建用户失败");

        return ResultUtils.success(user.getId());
    }

    /**
     * 【管理员】根据 ID 删除用户
     *
     * @param id 用户id
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @Operation(summary = "【管理员】根据 ID 删除用户")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> deleteUser(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        boolean result = userService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "删除用户失败");

        return ResultUtils.success(true);
    }

    /**
     * 【管理员】更新用户
     *
     * @param userUpdateRequest 更新用户请求体
     * @return 是否更新成功
     */
    @PostMapping("/update")
    @Operation(summary = "【管理员】更新用户")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);

        User user = BeanUtil.copyProperties(userUpdateRequest, User.class);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新用户失败");

        return ResultUtils.success(true);
    }

    /**
     * 【管理员】分页获取用户列表（脱敏）
     *
     * @param userQueryRequest 查询条件
     * @return 分页用户 VO 列表
     */
    @PostMapping("/list/page/vo")
    @Operation(summary = "【管理员】分页获取用户列表（脱敏）")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Page<UserVo>> listUserVoByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);

        Page<UserVo> userVoPage = userService.listUserVoByPage(userQueryRequest);
        return ResultUtils.success(userVoPage);
    }

    /**
     * 【管理员】根据 ID 获取用户（未脱敏）
     *
     * @param id 用户id
     * @return 用户完整信息
     */
    @GetMapping("/get")
    @Operation(summary = "【管理员】根据 ID 获取用户（未脱敏）")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<User> getUserById(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        return ResultUtils.success(user);
    }

    // ==================== 普通用户接口 ====================

    /**
     * 根据 ID 获取用户（脱敏）
     *
     * @param id 用户id
     * @return 用户 VO
     */
    @GetMapping("/get/vo")
    @Operation(summary = "根据 ID 获取用户（脱敏）")
    public BaseResponse<UserVo> getUserVoById(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        UserVo userVo = userService.getUserVoById(id);
        ThrowUtils.throwIf(userVo == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        return ResultUtils.success(userVo);
    }

}
