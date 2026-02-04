package com.yff.aicodemother.service;

import com.mybatisflex.core.service.IService;
import com.yff.aicodemother.model.entity.User;
import com.yff.aicodemother.model.vo.LoginVo;

/**
 * 用户 服务层。
 *
 * @author yff
 * @since 2026-02-02 15:36:30
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   账号
     * @param userPassword  密码
     * @param checkPassword 确认密码
     * @return 新用户id
     */
    Long userRegister(String userAccount, String userPassword, String checkPassword);


    /**
     * 用户登录
     *
     * @param userAccount  账号
     * @param userPassword 密码
     * @return 登录信息
     */
    LoginVo login(String userAccount, String userPassword);

    /**
     * 用户登出
     *
     * @param userId 用户id
     * @return 是否登出成功
     */
    Boolean logout(Long userId);
}
