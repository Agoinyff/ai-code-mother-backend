package com.yff.aicodemother.model.dto.user;


import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yff
 * @date 2026-02-04 14:11:22
 */
@Data
public class UserAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色: user/admin
     */
    private String userRole;

}
