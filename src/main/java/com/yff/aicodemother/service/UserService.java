package com.yff.aicodemother.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yff.aicodemother.model.dto.user.UserQueryRequest;
import com.yff.aicodemother.model.entity.User;
import com.yff.aicodemother.model.vo.LoginVo;
import com.yff.aicodemother.model.vo.UserVo;

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

    /**
     * 根据用户 ID 获取用户 VO（脱敏）
     *
     * @param userId 用户id
     * @return 用户 VO
     */
    UserVo getUserVoById(Long userId);

    /**
     * 分页查询用户列表（脱敏）
     *
     * @param userQueryRequest 查询条件
     * @return 分页用户 VO 列表
     */
    Page<UserVo> listUserVoByPage(UserQueryRequest userQueryRequest);


    /**
     * 获取加密后的密码
     *
     * @param userPassword 原始密码
     * @return 加密后的密码
     */
    String getEncryptedPassword(String userPassword);
}
