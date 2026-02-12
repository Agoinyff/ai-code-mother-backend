package com.yff.aicodemother.config;

import com.yff.aicodemother.interceptor.AuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author yff
 * @date 2026-02-04 10:20:32
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login", // 不是 /api/user/login
                        "/user/register",
                        "/doc.html/**",
                        "/static/**",
                        "/webjars/**",
                        "/v3/api-docs/**");

    }

    /**
     * 配置异步请求支持
     * 设置超时时间，防止长时间流式响应被中断
     */
    @Override
    public void configureAsyncSupport(
            AsyncSupportConfigurer configurer) {
        // 设置异步请求超时时间为 10 分钟（600000 毫秒）
        // 这对于长时间的 SSE 流式响应（如代码生成）是必要的
        configurer.setDefaultTimeout(600_000L);
    }
}
