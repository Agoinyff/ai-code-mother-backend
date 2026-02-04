package com.yff.aicodemother.interceptor;


import com.yff.aicodemother.common.login.UserHolder;
import com.yff.aicodemother.exception.BusinessException;
import com.yff.aicodemother.exception.ErrorCode;
import com.yff.aicodemother.exception.ThrowUtils;
import com.yff.aicodemother.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author yff
 * @date 2026-02-03 11:30:34
    springMVC的拦截器只拦截Controller层的请求，不拦截静态资源请求 ，即只拦截http请求
 */
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {


    //在请求处理之前进行拦截
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //每次请求进行的时候我需要先进行校验token是否合法
        String token = request.getHeader("access-token");
        ThrowUtils.throwIf(token == null, new BusinessException(ErrorCode.NO_AUTH_ERROR,"token不存在，请重新登录"));
        JwtUtil.isTokenValid(token);

        //解析token，拿到用户信息
        Long userId = JwtUtil.getUserId(token);
        UserHolder.setUser(userId); //保存到全局线程变量中



        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.clear(); //清除线程变量，防止内存泄漏
    }
}
