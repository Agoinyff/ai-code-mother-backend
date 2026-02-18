package com.yff.aicodemother.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yff.aicodemother.mapper.DeployHistoryMapper;
import com.yff.aicodemother.model.entity.DeployHistory;
import com.yff.aicodemother.service.DeployHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部署历史 服务层实现
 *
 * @author yff
 */
@Slf4j
@Service
public class DeployHistoryServiceImpl extends ServiceImpl<DeployHistoryMapper, DeployHistory>
        implements DeployHistoryService {

    @Autowired
    private DeployHistoryMapper deployHistoryMapper;

    @Override
    public DeployHistory recordDeploy(Long appId, String imageTag, String containerId,
            Integer containerPort, String deployUrl, Long userId) {
        // 获取下一个版本号
        Integer maxVersion = deployHistoryMapper.getMaxVersion(appId);
        int nextVersion = (maxVersion == null) ? 1 : maxVersion + 1;

        // 创建部署记录
        DeployHistory deployHistory = DeployHistory.builder()
                .appId(appId)
                .version(nextVersion)
                .imageTag(imageTag)
                .containerId(containerId)
                .containerPort(containerPort)
                .status("RUNNING")
                .deployUrl(deployUrl)
                .userId(userId)
                .build();

        this.save(deployHistory);
        log.info("记录部署历史 - 应用:{}, 版本:v{}, 镜像:{}, URL:{}", appId, nextVersion, imageTag, deployUrl);
        return deployHistory;
    }

    @Override
    public List<DeployHistory> getDeployVersions(Long appId) {
        return deployHistoryMapper.selectByAppId(appId);
    }

    @Override
    public void updateStatus(Long id, String status) {
        DeployHistory update = new DeployHistory();
        update.setId(id);
        update.setStatus(status);
        this.updateById(update);
        log.info("更新部署记录状态 - ID:{}, 新状态:{}", id, status);
    }

    @Override
    public DeployHistory getRunningDeploy(Long appId) {
        return this.getOne(new LambdaQueryWrapper<DeployHistory>()
                .eq(DeployHistory::getAppId, appId)
                .eq(DeployHistory::getStatus, "RUNNING")
                .orderByDesc(DeployHistory::getVersion)
                .last("LIMIT 1"));
    }
}
