package com.yff.aicodemother.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yff
 * @date 2026-02-04 11:12:03
 */
@Target(ElementType.METHOD) //  // 该注解用于方法上
@Retention(RetentionPolicy.RUNTIME)  //  // 运行时有效
public @interface AuthCheck {


    /*
        必须角色
     */
    String mustRole() default "";


}
