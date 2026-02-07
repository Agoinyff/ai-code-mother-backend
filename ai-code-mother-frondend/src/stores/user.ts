import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserVo } from '@/types/user'
import { STORAGE_KEYS } from '@/config/api'
import * as userApi from '@/api/user'

export const useUserStore = defineStore('user', () => {
    // 状态
    const userInfo = ref<UserVo | null>(null)
    const token = ref<string | null>(localStorage.getItem(STORAGE_KEYS.TOKEN))

    // 计算属性
    const isLoggedIn = computed(() => !!token.value && !!userInfo.value)
    const isAdmin = computed(() => userInfo.value?.userRole === 'admin')

    // 初始化用户信息
    async function initUserInfo(): Promise<boolean> {
        // 如果已经有 userInfo，不需要重新获取
        if (userInfo.value) {
            return true
        }

        // 没有 token，直接返回
        if (!token.value) {
            return false
        }

        try {
            const res = await userApi.getLoginUser()
            userInfo.value = res.data
            return true
        } catch (error) {
            // token 无效，清除登录状态（不抛出错误）
            console.warn('Token 无效，已清除登录状态')
            clearAuth()
            return false
        }
    }

    // 登录
    async function login(userAccount: string, userPassword: string) {
        const res = await userApi.login({ userAccount, userPassword })
        token.value = res.data.token
        userInfo.value = res.data.userVo

        // 存储到 localStorage
        localStorage.setItem(STORAGE_KEYS.TOKEN, res.data.token)
        localStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(res.data.userVo))
    }

    // 登出
    async function logout() {
        try {
            await userApi.logout()
        } catch (error) {
            // 即使请求失败也清除本地状态
        }
        clearAuth()
    }

    // 清除认证状态
    function clearAuth() {
        token.value = null
        userInfo.value = null
        localStorage.removeItem(STORAGE_KEYS.TOKEN)
        localStorage.removeItem(STORAGE_KEYS.USER_INFO)
    }

    // 注册
    async function register(userAccount: string, userPassword: string, checkPassword: string) {
        const res = await userApi.register({ userAccount, userPassword, checkPassword })
        return res.data
    }

    return {
        // 状态
        userInfo,
        token,
        // 计算属性
        isLoggedIn,
        isAdmin,
        // 方法
        initUserInfo,
        login,
        logout,
        register,
        clearAuth,
    }
})
