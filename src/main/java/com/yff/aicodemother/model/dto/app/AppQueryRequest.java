package com.yff.aicodemother.model.dto.app;

import com.yff.aicodemother.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户分页查询应用请求（支持名称查询，每页最多20个）
 *
 * @author yff
 * @since 2026-02-06
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用名称（模糊查询）
     */
    private String appName;

    /**
     * 限制每页最大数量为20
     */
    @Override
    public int getPageSize() {
        int size = super.getPageSize();
        return Math.min(size, 20);
    }

}
