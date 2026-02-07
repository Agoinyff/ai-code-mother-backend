package com.yff.aicodemother.ai.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.yff.aicodemother.ai.model.HtmlCodeResult;
import com.yff.aicodemother.ai.model.MultiFileCodeResult;
import com.yff.aicodemother.ai.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author yff
 * @date 2026-02-05 13:53:39
 * 代码文件保存器工具类
 */
@Deprecated
public class CodeFileSaver {

    // 文件保存跟目录 System.getProperty("user.dir") 获取 当前工作目录（即项目运行时的根目录）
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";


    /**
     * 保存HTML代码结果到文件系统
     *
     * @param result HTML代码结果对象
     * @return 保存代码的目录对象
     */
    @Deprecated
    public static File saveHtmlCodeResult(HtmlCodeResult result) {

        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        return new File(baseDirPath); //返回保存代码的目录对象

    }

    /**
     * 保存多文件代码结果到文件系统
     *
     * @param result 多文件代码结果对象
     * @return 保存代码的目录对象
     */
    @Deprecated
    public static File saveMultiFileCodeResult(MultiFileCodeResult result) {

        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        writeToFile(baseDirPath, "styles.css", result.getCssCode());
        writeToFile(baseDirPath, "script.js", result.getJsCode());
        return new File(baseDirPath); //返回保存代码的目录对象

    }


    /**
     * 写入单个文件
     *
     * @param dirPath  目录路径
     * @param fileName 文件名
     * @param content  内容
     */
    @Deprecated
    private static void writeToFile(String dirPath, String fileName, String content) {

        String filePath = dirPath + File.separator + fileName;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }


    /**
     * 构建唯一的目录路径 : tmp/code_output/{bizType}_雪花ID
     *
     * @param bizType 业务类型
     * @return 唯一目录路径
     */
    @Deprecated
    private static String buildUniqueDir(String bizType) {


        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());  //这里使用的是hutool的StrUtil和IdUtil生成雪花ID
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName; //File.separator 是 Java 中表示操作系统文件路径分隔符的常量。  防止硬编码导致的各种问题
        FileUtil.mkdir(dirPath); //创建目录
        return dirPath;
    }


}
