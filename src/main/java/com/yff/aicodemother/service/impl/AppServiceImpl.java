package com.yff.aicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yff.aicodemother.ai.core.AiCodeGeneratorFacade;
import com.yff.aicodemother.ai.model.enums.CodeGenTypeEnum;
import com.yff.aicodemother.constant.AppConstant;
import com.yff.aicodemother.exception.BusinessException;
import com.yff.aicodemother.exception.ErrorCode;
import com.yff.aicodemother.exception.ThrowUtils;
import com.yff.aicodemother.mapper.AppMapper;
import com.yff.aicodemother.model.dto.app.AppAdminQueryRequest;
import com.yff.aicodemother.model.dto.app.AppAdminUpdateRequest;
import com.yff.aicodemother.model.dto.app.AppAddRequest;
import com.yff.aicodemother.model.dto.app.AppQueryRequest;
import com.yff.aicodemother.model.dto.app.AppUpdateRequest;
import com.yff.aicodemother.model.entity.App;
import com.yff.aicodemother.model.entity.User;
import com.yff.aicodemother.model.vo.AppVo;
import com.yff.aicodemother.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;

/**
 * 应用 服务层实现。
 *
 * @author yff
 * @since 2026-02-06
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Autowired
    private AppMapper appMapper;

    @Autowired
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Override
    public Long createApp(AppAddRequest appAddRequest, Long userId) {
        // 校验参数
        if (StrUtil.isBlank(appAddRequest.getInitPrompt())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "initPrompt 不能为空");
        }
        if (StrUtil.isBlank(appAddRequest.getAppName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用名称不能为空");
        }

        // 创建应用
        App app = BeanUtil.copyProperties(appAddRequest, App.class);
        app.setUserId(userId);
        app.setPriority(0); // 默认优先级为0，管理员可设置为99表示精选

        boolean result = this.save(app);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建应用失败");
        }
        return app.getId();
    }

    @Override
    public Boolean updateMyApp(AppUpdateRequest appUpdateRequest, Long userId) {
        Long appId = appUpdateRequest.getId();
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID不合法");
        }

        // 校验应用是否存在且属于当前用户
        App app = this.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        if (!app.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权修改该应用");
        }

        // 更新应用名称
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setAppName(appUpdateRequest.getAppName());

        return this.updateById(updateApp);
    }

    @Override
    public Boolean deleteMyApp(Long appId, Long userId) {
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID不合法");
        }

        // 校验应用是否存在且属于当前用户
        App app = this.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        if (!app.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权删除该应用");
        }

        return this.removeById(appId);
    }

    @Override
    public AppVo getAppVoById(Long appId) {
        App app = this.getById(appId);
        if (app == null) {
            return null;
        }
        return BeanUtil.copyProperties(app, AppVo.class);
    }

    @Override
    public Page<AppVo> listMyAppVoByPage(AppQueryRequest appQueryRequest, Long userId) {
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = appQueryRequest.getPageSize();

        IPage<AppVo> appVoPage = appMapper.selectMyAppVoPage(new Page<>(pageNum, pageSize), appQueryRequest, userId);
        return (Page<AppVo>) appVoPage;
    }

    @Override
    public Page<AppVo> listFeaturedAppVoByPage(AppQueryRequest appQueryRequest) {
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = appQueryRequest.getPageSize();

        IPage<AppVo> appVoPage = appMapper.selectFeaturedAppVoPage(new Page<>(pageNum, pageSize), appQueryRequest);
        return (Page<AppVo>) appVoPage;
    }

    @Override
    public Boolean adminDeleteApp(Long appId) {
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID不合法");
        }

        App app = this.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }

        return this.removeById(appId);
    }

    @Override
    public Boolean adminUpdateApp(AppAdminUpdateRequest appAdminUpdateRequest) {
        Long appId = appAdminUpdateRequest.getId();
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID不合法");
        }

        App app = this.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }

        // 更新应用（名称、封面、优先级、是否精选）
        App updateApp = BeanUtil.copyProperties(appAdminUpdateRequest, App.class);
        return this.updateById(updateApp);
    }

    @Override
    public Page<AppVo> adminListAppVoByPage(AppAdminQueryRequest appAdminQueryRequest) {
        int pageNum = appAdminQueryRequest.getPageNum();
        int pageSize = appAdminQueryRequest.getPageSize();

        IPage<AppVo> appVoPage = appMapper.selectAppVoPageForAdmin(new Page<>(pageNum, pageSize), appAdminQueryRequest);
        return (Page<AppVo>) appVoPage;
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String userMessage, User user) {
        //校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不合法");
        ThrowUtils.throwIf(userMessage == null || userMessage.isEmpty(), ErrorCode.PARAMS_ERROR, "用户消息不能为空");

        //查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        //验证用户权限（公开应用或创建者本人可访问）
        if (!app.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权访问该应用");
        }
        //获取应用的代码生成类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用的代码生成类型不合法");
        }

        return aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, codeGenTypeEnum, appId);


    }


    @Override
    public String deployApp(Long appId, User loginUser) {
        //参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不合法");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        //查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        //验证用户权限（公开应用或创建者本人可访问）
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权部署该应用");
        }
        //检查是否已有deployKey，没有则随机生成一个
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)){
            deployKey = RandomUtil.randomString(6); //生成6位随机字符串包含数字和字母
        }
        //获取代码生成类型，构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType+"_"+appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        //检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，无法部署,请先生成代码");
        }

        //目录存在则复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir,new File(deployDirPath),true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署应用失败:"+ e.getMessage());
        }

        //更新应用的deployKey和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);

        ThrowUtils.throwIf(!updateResult, ErrorCode.SYSTEM_ERROR, "更新应用部署信息失败");

        //构建部署URL并返回
        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }

}
