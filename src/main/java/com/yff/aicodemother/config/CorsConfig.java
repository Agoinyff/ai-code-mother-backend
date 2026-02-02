package com.yff.aicodemother.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author yff
 * @date 2026-02-02 10:16:34
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //覆盖所有的请求
        registry.addMapping("/**")
                .allowedOriginPatterns("*") //允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") //允许的HTTP方法
                .allowedHeaders("*") //允许所有请求头
                .allowCredentials(false) //是否允许发送Cookie
                .exposedHeaders("*");    //允许客户端访问的响应头
    }
}
