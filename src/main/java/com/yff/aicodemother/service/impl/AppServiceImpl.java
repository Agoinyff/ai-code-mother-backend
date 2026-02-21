package com.yff.aicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yff.aicodemother.ai.core.AiCodeGeneratorFacade;
import com.yff.aicodemother.ai.core.builder.VueProjectBuilder;
import com.yff.aicodemother.ai.core.handler.StreamHandlerExecutor;
import com.yff.aicodemother.ai.model.enums.CodeGenTypeEnum;
import com.yff.aicodemother.ai.routing.AiCodeGenTypeRoutingService;
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
import com.yff.aicodemother.model.dto.chathistory.ChatHistoryAddRequest;
import com.yff.aicodemother.model.entity.App;
import com.yff.aicodemother.model.entity.DeployHistory;
import com.yff.aicodemother.model.entity.User;
import com.yff.aicodemother.model.enums.MessageTypeEnum;
import com.yff.aicodemother.model.vo.AppVo;
import com.yff.aicodemother.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * 应用 服务层实现。
 *
 * @author yff
 * @since 2026-02-06
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Autowired
    private AppMapper appMapper;

    @Autowired
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Autowired
    private StreamHandlerExecutor streamHandlerExecutor;
    @Autowired
    private VueProjectBuilder vueProjectBuilder;
    @Autowired
    private DockerContainerService dockerContainerService;
    @Autowired
    private DeployHistoryService deployHistoryService;

    @Autowired
    private ScreenshotService screenshotService;

    @Autowired
    private AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService;

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

        // 级联删除该应用的所有对话历史
        chatHistoryService.deleteByAppId(appId);

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

        // 级联删除该应用的所有对话历史
        chatHistoryService.deleteByAppId(appId);

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
        // 校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不合法");
        ThrowUtils.throwIf(userMessage == null || userMessage.isEmpty(), ErrorCode.PARAMS_ERROR, "用户消息不能为空");

        // 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 验证用户权限（公开应用或创建者本人可访问）
        if (!app.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权访问该应用");
        }
        // 获取应用的代码生成类型（支持 AI 自动路由）
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum;

        if (StrUtil.isBlank(codeGenType) || "auto".equals(codeGenType)) {
            // AI 自动路由：根据用户 prompt 智能判断代码生成类型
            log.info("应用 {} 使用AI自动路由判断代码生成类型, prompt: {}", appId, userMessage);
            codeGenTypeEnum = aiCodeGenTypeRoutingService.routeCodeGenType(userMessage);
            log.info("应用 {} AI路由结果: {}", appId, codeGenTypeEnum);
            // 回写到 App 记录，后续对话沿用此类型
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCodeGenType(codeGenTypeEnum.getValue());
            this.updateById(updateApp);
        } else {
            codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
            if (codeGenTypeEnum == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用的代码生成类型不合法");
            }
        }

        // 1. 保存用户消息到对话历史
        ChatHistoryAddRequest userHistoryRequest = new ChatHistoryAddRequest();
        userHistoryRequest.setAppId(appId);
        userHistoryRequest.setUserId(user.getId());
        userHistoryRequest.setMessage(userMessage);
        userHistoryRequest.setMessageType(MessageTypeEnum.USER.getValue());
        chatHistoryService.saveChatMessage(userHistoryRequest);

        // 2. VUE_PROJECT 类型：将源文件内容注入到 prompt 中，帮助 AI 定位需要修改的组件
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            String sourceContext = readVueSourceFiles(appId);
            if (StrUtil.isNotBlank(sourceContext)) {
                userMessage = sourceContext + "\n\n" + userMessage;
                log.info("已为 VUE_PROJECT 应用 {} 注入源文件上下文", appId);
            }
        }

        // 3. 生成代码流并保存 AI 响应
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, codeGenTypeEnum, appId);

        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, user, codeGenTypeEnum);

    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不合法");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 验证用户权限
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权部署该应用");
        }

        // ===== Docker 容器化部署 =====
        if (dockerContainerService.isDockerAvailable()) {
            log.info("Docker 可用，使用容器化部署 - 应用:{}", appId);
            // Vue 项目在 AI 代码生成完成后已由 JsonMessageStreamHandler.doOnComplete 异步构建，
            // 部署时 dist 目录已就绪，无需重复构建。

            // 将旧的运行中的部署记录标记为 STOPPED
            DeployHistory runningDeploy = deployHistoryService.getRunningDeploy(appId);
            if (runningDeploy != null) {
                deployHistoryService.updateStatus(runningDeploy.getId(), "STOPPED");
            }

            // 执行 Docker 部署
            String deployUrl = dockerContainerService.deployAsContainer(appId);

            // 记录部署版本历史
            String imageTag = "acm-deploy-" + appId + ":v" + System.currentTimeMillis();
            deployHistoryService.recordDeploy(appId, imageTag, null, null, deployUrl, loginUser.getId());

            // 更新应用部署时间
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setDeployedTime(LocalDateTime.now());
            updateApp.setDeployKey(deployUrl); // 存储部署 URL 到 deployKey 字段
            this.updateById(updateApp);

            // 异步生成截图并更新应用封面
            screenshotService.captureAndUpdateCoverAsync(appId, deployUrl);

            return deployUrl;
        }

        // ===== 降级方案：文件复制部署（Docker 不可用时） =====
        log.warn("Docker 不可用，降级为文件复制部署 - 应用:{}", appId);
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }

        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，无法部署，请先生成代码");
        }

        CodeGenTypeEnum enumByValue = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (enumByValue == CodeGenTypeEnum.VUE_PROJECT) {
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue项目构建失败，无法部署");
            File distDir = new File(sourceDir, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue项目构建完成但是未生成dist目录");
            sourceDir = distDir;
        }

        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署应用失败:" + e.getMessage());
        }

        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        this.updateById(updateApp);

        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }

    @Override
    public String startPreview(Long appId, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不合法");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作该应用");
        }

        // 直接返回静态文件 URL（StaticResourceController 会自动处理 dist 子目录）
        // 格式：/static/{codeGenType}_{appId}/
        String previewKey = app.getCodeGenType() + "_" + appId;
        String previewUrl = AppConstant.CODE_DEPLOY_HOST + "/static/" + previewKey + "/";
        log.info("应用 {} 预览 URL: {}", appId, previewUrl);
        return previewUrl;
    }

    @Override
    public void stopPreview(Long appId, User loginUser) {
        // 静态文件预览无需停止操作（无容器资源需要释放）
        log.info("应用 {} 停止预览（静态文件模式，无需操作）", appId);
    }

    @Override
    public List<DeployHistory> getDeployVersions(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不合法");
        return deployHistoryService.getDeployVersions(appId);
    }

    @Override
    public String rollbackDeploy(Long appId, Integer version, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不合法");
        ThrowUtils.throwIf(version == null || version <= 0, ErrorCode.PARAMS_ERROR, "版本号不合法");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作该应用");
        }

        // 查找目标版本的部署记录
        List<DeployHistory> versions = deployHistoryService.getDeployVersions(appId);
        DeployHistory targetVersion = versions.stream()
                .filter(v -> v.getVersion().equals(version))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "部署版本 v" + version + " 不存在"));

        // 如果目标版本已经是 RUNNING，无需回滚
        if ("RUNNING".equals(targetVersion.getStatus())) {
            return targetVersion.getDeployUrl();
        }

        // 将当前运行中的部署标记为 STOPPED
        DeployHistory runningDeploy = deployHistoryService.getRunningDeploy(appId);
        if (runningDeploy != null) {
            deployHistoryService.updateStatus(runningDeploy.getId(), "STOPPED");
        }

        // 直接将目标版本状态改为 RUNNING（不新增版本记录）
        deployHistoryService.updateStatus(targetVersion.getId(), "RUNNING");

        String deployUrl = targetVersion.getDeployUrl();
        log.info("应用 {} 回滚到版本 v{} 成功, URL: {}", appId, version, deployUrl);
        return deployUrl;
    }

    @Override
    public void stopDeploy(Long appId, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不合法");
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作该应用");
        }
        // 停止 Docker 部署容器
        dockerContainerService.stopDeployContainer(appId);
        // 将 RUNNING 状态的部署记录标记为 STOPPED
        DeployHistory runningDeploy = deployHistoryService.getRunningDeploy(appId);
        if (runningDeploy != null) {
            deployHistoryService.updateStatus(runningDeploy.getId(), "STOPPED");
        }
        // 清空 App 的 deployKey（表示已下线）
        // 注意：MyBatis-Plus updateById 默认忽略 null 值，需用 UpdateWrapper 显式置空
        // 列名为 deployKey（与 @TableField("deployKey") 一致）
        this.update(new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<App>()
                .set("deployKey", null)
                .eq("id", appId));
        log.info("应用 {} 部署容器已停止下线", appId);
    }

    /**
     * 读取 Vue 项目 src/ 目录下的所有源文件内容，用于注入到 AI prompt
     * 读取范围：.vue / .js / .css / .json 文件
     *
     * @param appId 应用ID
     * @return 格式化的源文件内容字符串，若无源文件则返回空字符串
     */
    private String readVueSourceFiles(Long appId) {
        String projectDirName = "vue_project_" + appId;
        Path srcDir = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName, "src");

        if (!Files.exists(srcDir) || !Files.isDirectory(srcDir)) {
            log.warn("Vue 项目 src 目录不存在: {}", srcDir);
            return "";
        }

        List<String> extensions = Arrays.asList(".vue", ".js", ".css", ".json");
        StringBuilder sb = new StringBuilder();
        sb.append("[当前项目源文件，请根据以下源码定位需要修改的文件]\n");

        try (Stream<Path> walk = Files.walk(srcDir)) {
            Path projectRoot = srcDir.getParent(); // vue_project_{appId}
            walk.filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return extensions.stream().anyMatch(name::endsWith);
                    })
                    .sorted()
                    .forEach(filePath -> {
                        try {
                            // 使用相对于项目根目录的路径，如 src/App.vue
                            String relativePath = projectRoot.relativize(filePath).toString().replace('\\', '/');
                            String content = Files.readString(filePath, StandardCharsets.UTF_8);
                            // 转义 {{ 和 }}，避免 langchain4j 将 Vue 模板语法误认为 prompt 模板变量
                            content = content.replace("{{", "{ {").replace("}}", "} }");
                            sb.append("\n--- ").append(relativePath).append(" ---\n");
                            sb.append(content).append("\n");
                        } catch (IOException e) {
                            log.warn("读取源文件失败: {}", filePath, e);
                        }
                    });
        } catch (IOException e) {
            log.error("遍历 Vue 项目源文件目录失败: {}", srcDir, e);
            return "";
        }

        return sb.toString();
    }

}
