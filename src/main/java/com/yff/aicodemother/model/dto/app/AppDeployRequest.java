package com.yff.aicodemother.model.dto.app;


import lombok.Data;

import java.io.Serializable;

/**
 *
 * 应用部署请求类
 *
 * @author yff
 * @date 2026-02-07 14:24:26
 */
@Data
public class AppDeployRequest implements Serializable {



    // 应用 id
    private Long appId;


    private static final long serialVersionUID = 1L;

}
