package com.yff.aicodemother;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})  //排除Embedding向量的自动配置类
@EnableAspectJAutoProxy(proxyTargetClass = true) //开启AOP对象代理
@MapperScan("com.yff.aicodemother.mapper") //扫描Mapper接口所在的包
@EnableAsync
public class AiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCodeMotherApplication.class, args);
    }

}
