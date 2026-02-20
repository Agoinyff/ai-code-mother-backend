package com.yff.aicodemother.manager;


import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.yff.aicodemother.common.PageRequest;
import com.yff.aicodemother.config.CosClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 *
 * 专门负责和cos对象存储进行交互的管理器，封装cos的上传、下载、删除等操作，提供给service层调用。
 *
 * @author yff
 * @date 2026-02-20 09:29:06
 */
@Component
@Slf4j
public class CosManager {


    @Autowired
    private CosClientConfig cosClientConfig;

    @Autowired
    private COSClient cosClient;


    /**
     * 上传文件到cos对象存储
     * @param key 文件在cos中的唯一标识（相当于路径）
     * @param file 要上传的文件对象
     * @return 上传结果对象，包含文件的ETag、版本ID等信息
     */
    public PutObjectResult putObjectResult(String key, File file){

        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);


    }

    /**
     * 上传文件到cos对象存储，并返回访问url
     * @param key 文件在cos中的唯一标识（相当于路径）
     * @param file 要上传的文件对象
     * @return 上传成功后文件的访问url，如果上传失败则返回null
     */
    public String uploadFile(String key,File file){
        //上传文件
        PutObjectResult result = putObjectResult(key, file);
        if (result!=null){
            //构建访问url
            String url = String.format("%s%s",cosClientConfig.getHost(),key);
            log.info("文件上传成功 - name: {}, url: {}", file.getName(), url);
            return url;
        }else {
            log.error("文件上传失败 - name: {},返回结果为空", file.getName());
            return null;
        }
    }




}
