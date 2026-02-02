package com.yff.aicodemother.model.dto.user;


import lombok.Data;

import java.io.Serializable;

/**
 * @author yff
 * @date 2026-02-02 17:00:07
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

}
