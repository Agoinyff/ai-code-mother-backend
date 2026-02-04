package com.yff.aicodemother.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户 实体类。
 *
 * @author yff
 * @since 2026-02-02 15:36:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID) // 使用雪花算法生成ID
    private Long id;

    /**
     * 账号
     */
    @TableField("userAccount")
    private String userAccount;

    /**
     * 密码
     */
    @TableField("userPassword")
    private String userPassword;

    /**
     * 用户昵称
     */
    @TableField("userName")
    private String userName;

    /**
     * 用户头像
     */
    @TableField("userAvatar")
    private String userAvatar;

    /**
     * 用户简介
     */
    @TableField("userProfile")
    private String userProfile;

    /**
     * 用户角色: user/admin
     */
    @TableField("userRole")
    private String userRole;

    /**
     * 分享码
     */
    @TableField("shareCode")
    private String shareCode;

    /**
     * 邀请用户 id
     */
    @TableField("inviteUser")
    private Long inviteUser;

    /**
     * 会员过期时间
     */
    @TableField("vipExpireTime")
    private LocalDateTime vipExpireTime;

    /**
     * 会员兑换码
     */
    @TableField("vipCode")
    private String vipCode;

    /**
     * 会员编号
     */
    @TableField("vipNumber")
    private Long vipNumber;

    /**
     * 编辑时间
     */
    @TableField("editTime")
    private LocalDateTime editTime;

    /**
     * 创建时间
     */
    @TableField("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @TableField("isDelete")
    @TableLogic
    private Integer isDelete;

}
