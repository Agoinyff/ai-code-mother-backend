package com.yff.aicodemother.ai.core.saver;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.yff.aicodemother.ai.model.enums.CodeGenTypeEnum;
import com.yff.aicodemother.exception.BusinessException;
import com.yff.aicodemother.exception.ErrorCode;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 抽象的代码文件保存器模板  - 模板方法模式
 *
 * @author yff
 * @date 2026-02-06 09:38:32
 */
public abstract class CodeFileSaverTemplate<T> {

    // 文件保存跟目录 System.getProperty("user.dir") 获取 当前工作目录（即项目运行时的根目录）
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";


    /**
     * 保存代码到文件系统的模板方法,标准流程
     *
     * @param result 代码结果对象
     * @return 保存代码的目录对象
     */
    public final File saveCode(T result) {
        //1.验证输入
        validateInput(result);
        //2.构建唯一目录
        String baseDirPath = buildUniqueDir();
        //3.保存代码文件(具体实现由子类完成)
        saveCodeFiles(result, baseDirPath);
        //4.返回保存代码的目录对象
        return new File(baseDirPath);

    }


    /**
     * 构建唯一目录
     *
     * @return 目录路径
     */
    private String buildUniqueDir() {
        String codeType = getCodeType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}", codeType, IdUtil.getSnowflakeNextIdStr());  //这里使用的是hutool的StrUtil和IdUtil生成雪花ID
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName; //File.separator 是 Java 中表示操作系统文件路径分隔符的常量。  防止硬编码导致的各种问题
        FileUtil.mkdir(dirPath); //创建目录
        return dirPath;
    }

    /**
     * 写入单个文件
     *
     * @param dirPath  目录路径
     * @param fileName 文件名
     * @param content  内容
     */
    protected final void writeToFile(String dirPath, String fileName, String content) {
        if (StrUtil.isNotBlank(content)) {

            String filePath = dirPath + File.separator + fileName;
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }



    /**
     * 验证输入参数（可由子类覆盖）
     *
     * @param result 代码结果对象
     */
    protected void validateInput(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码结果对象不能为空");
        }
    }


    /**
     * 保存代码文件（由子类实现具体保存逻辑）
     * @param result 代码结果对象
     * @param baseDirPath 目录路径
     */
    protected abstract void saveCodeFiles(T result, String baseDirPath) ;


    /**
     * 获取代码生成类型（由子类实现具体类型）
     *
     * @return 代码生成类型枚举
     */
    protected abstract CodeGenTypeEnum getCodeType();

}
