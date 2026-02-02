package com.yff.aicodemother.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yff
 * @date 2026-02-02 15:45:52
 */
@Getter
public enum UserRoleEnum {

    USER("用户", "user"),
    ADMIN("管理员", "admin");



    private final String text;
    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }



    /*
        如果这里枚举值特别的多的话，可以在类加载的时候缓存所有枚举值到map中，避免每次查找都遍历一遍枚举值，提升性能。
     */
//    // ✅ 静态 Map 缓存，只初始化一次
//    private static final Map<String, UserRoleEnum> VALUE_MAP;
//    static {
//        // 类加载时构建缓存
//        VALUE_MAP = new HashMap<>();
//        for (UserRoleEnum e : values()) {
//            VALUE_MAP.put(e.value, e);
//        }
//    }
//    // ✅ 优化后：O(1) 查找
//    public static UserRoleEnum getEnumByValue(String value) {
//        if (ObjUtil.isEmpty(value)) {
//            return null;
//        }
//        return VALUE_MAP.get(value);  // HashMap 查找，O(1)
//    }






    //枚举类常见设计模式

    //通过value值找到对应的枚举
    public static UserRoleEnum getEnumByValue(String value){
        if (ObjUtil.isEmpty(value)){
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.value.equals(value)){
                return userRoleEnum;
            }
        }
        return null;
    }

    //通过text值找到对应的枚举
    public static UserRoleEnum getEnumByText(String text){
        if (ObjUtil.isEmpty(text)){
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.text.equals(text)){
                return userRoleEnum;
            }
        }
        return null;
    }

}
