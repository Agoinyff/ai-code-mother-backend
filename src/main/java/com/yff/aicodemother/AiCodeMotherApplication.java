package com.yff.aicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true) //开启AOP对象代理
@MapperScan("com.yff.aicodemother.mapper") //扫描Mapper接口所在的包
public class AiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCodeMotherApplication.class, args);
    }

}
