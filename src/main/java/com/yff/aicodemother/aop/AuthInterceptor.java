package com.yff.aicodemother.aop;


import com.yff.aicodemother.annotation.AuthCheck;
import com.yff.aicodemother.common.login.UserHolder;
import com.yff.aicodemother.exception.BusinessException;
import com.yff.aicodemother.exception.ErrorCode;
import com.yff.aicodemother.model.enums.UserRoleEnum;
import com.yff.aicodemother.model.vo.UserVo;
import com.yff.aicodemother.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yff
 * @date 2026-02-04 11:16:06
 * 方法调用（带 @AuthCheck 注解）
 *         ↓
 * 获取注解的 mustRole 值
 *         ↓
 *     mustRole 为空？ ────是──→ 直接放行 ✅
 *         ↓ 否
 * 获取当前用户角色
 *         ↓
 *     用户角色为空？ ────是──→ 拒绝访问 ❌
 *         ↓ 否
 * 要求 admin 但用户不是 admin？ ────是──→ 拒绝访问 ❌
 *         ↓ 否
 *       放行 ✅
 */
@Aspect
@Component
public class AuthInterceptor {

    @Autowired
    private UserService userService;


    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {

        String mustRole = authCheck.mustRole();

        Long userId = UserHolder.getUserId();

        //获取当前用户
        UserVo loginUser = userService.getUserVoById(userId);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);

        //不需要权限就放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }

        //以下为必须有权限才通过
        //获取当前用户的权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());

        //获取不到则拒绝
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        //权限不足则拒绝
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        return joinPoint.proceed();
    }


}
