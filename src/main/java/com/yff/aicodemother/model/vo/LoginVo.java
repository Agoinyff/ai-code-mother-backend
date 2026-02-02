package com.yff.aicodemother.model.vo;


import lombok.Data;

import java.io.Serializable;

/**
 * @author yff
 * @date 2026-02-02 17:24:34
 */
@Data
public class LoginVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户信息
     */
    private UserVo userVo;

    /**
     * 令牌
     */
    private String token;

}
