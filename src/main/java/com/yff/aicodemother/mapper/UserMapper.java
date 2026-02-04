package com.yff.aicodemother.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yff.aicodemother.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 映射层。
 *
 * @author yff
 * @since 2026-02-02 15:36:30
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
