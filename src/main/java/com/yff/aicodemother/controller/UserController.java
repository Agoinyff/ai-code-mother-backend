package com.yff.aicodemother.controller;

import com.yff.aicodemother.common.BaseResponse;
import com.yff.aicodemother.common.ResultUtils;
import com.yff.aicodemother.common.login.UserHolder;
import com.yff.aicodemother.exception.ErrorCode;
import com.yff.aicodemother.exception.ThrowUtils;
import com.yff.aicodemother.model.dto.user.UserLoginRequest;
import com.yff.aicodemother.model.dto.user.UserRegisterRequest;
import com.yff.aicodemother.model.vo.LoginVo;
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

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public BaseResponse<LoginVo> userLogin(@RequestBody UserLoginRequest userLoginRequest) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginVo loginVo = userService.login(userAccount, userPassword);

        //返回jwt生成的token
        return ResultUtils.success(loginVo);
    }


    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public BaseResponse<Boolean> userLogout() {
        Boolean result = userService.logout(UserHolder.getUserId());
        return ResultUtils.success(result);
    }

}
