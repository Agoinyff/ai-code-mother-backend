package com.yff.aicodemother.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yff.aicodemother.model.entity.DeployHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 部署历史 Mapper 接口
 *
 * @author yff
 */
@Mapper
public interface DeployHistoryMapper extends BaseMapper<DeployHistory> {

    /**
     * 获取某应用当前最大版本号
     *
     * @param appId 应用ID
     * @return 最大版本号，如果无部署记录则返回 null
     */
    @Select("SELECT MAX(version) FROM deploy_history WHERE app_id = #{appId}")
    Integer getMaxVersion(@Param("appId") Long appId);

    /**
     * 查询某应用的部署历史，按版本号降序排列
     *
     * @param appId 应用ID
     * @return 部署历史列表
     */
    @Select("SELECT * FROM deploy_history WHERE app_id = #{appId} ORDER BY version DESC")
    List<DeployHistory> selectByAppId(@Param("appId") Long appId);

}
