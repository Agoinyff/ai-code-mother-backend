package com.yff.aicodemother.config;


import com.yff.aicodemother.interceptor.AuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
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
                        "/user/login",                   // 不是 /api/user/login
                        "/user/register",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**"
                );

    }
}
