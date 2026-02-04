package com.yff.aicodemother.model.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yff
 * @date 2026-02-02 17:22:50
 */
@Data
public class UserVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    @Column("userAccount")
    private String userAccount;

    /**
     * 用户昵称
     */
    @Column("userName")
    private String userName;

    /**
     * 用户头像
     */
    @Column("userAvatar")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Column("userProfile")
    private String userProfile;

    /**
     * 用户角色: user/admin
     */
    @Column("userRole")
    private String userRole;

    /**
     * 分享码
     */
    @Column("shareCode")
    private String shareCode;

    /**
     * 邀请用户 id
     */
    @Column("inviteUser")
    private Long inviteUser;

    /**
     * 会员过期时间
     */
    @Column("vipExpireTime")
    private LocalDateTime vipExpireTime;

    /**
     * 会员兑换码
     */
    @Column("vipCode")
    private String vipCode;

    /**
     * 会员编号
     */
    @Column("vipNumber")
    private Long vipNumber;


    /**
     * 创建时间
     */
    @Column("createTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;





}
