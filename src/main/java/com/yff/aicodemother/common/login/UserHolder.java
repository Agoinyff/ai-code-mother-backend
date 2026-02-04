package com.yff.aicodemother.common.login;


/**
 * @author yff
 * @date 2026-02-04 10:13:14
 *
 */
public class UserHolder {

   private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();


   /**
    * 存储用户信息到当前线程
    *
    * @param id 用户信息
    */
   public static void setUser(Long id) {
       threadLocal.set(id);
   }

   /**
    * 获取当前线程存储的用户信息
    * @return 用户信息
    */
    public static Long getUserId() {
         return threadLocal.get();
    }

    /**
     * 清除当前线程存储的用户信息
     */
    public static void clear() {
        threadLocal.remove();
    }


}
