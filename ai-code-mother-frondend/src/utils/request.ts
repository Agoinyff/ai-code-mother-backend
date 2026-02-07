import axios, { type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage, ElNotification } from 'element-plus'
import { API_CONFIG, STORAGE_KEYS } from '@/config/api'
import type { BaseResponse } from '@/types/common'
import router from '@/router'

// 创建 axios 实例
const request: AxiosInstance = axios.create({
    baseURL: API_CONFIG.BASE_URL,
    timeout: API_CONFIG.TIMEOUT,
    headers: {
        'Content-Type': 'application/json',
    },
})

// 错误码映射
const ERROR_CODE_MAP: Record<number, string> = {
    40000: '请求参数错误',
    40100: '未登录或登录已过期',
    40101: '账号或密码错误',
    40300: '无权限访问',
    40400: '请求的资源不存在',
    50000: '服务器内部错误',
    50001: 'AI 服务暂时不可用',
}

// HTTP 状态码映射
const HTTP_STATUS_MAP: Record<number, string> = {
    400: '请求参数错误',
    401: '未登录或登录已过期',
    403: '无权限访问',
    404: '请求的资源不存在',
    408: '请求超时',
    500: '服务器内部错误',
    502: '网关错误',
    503: '服务暂时不可用',
    504: '网关超时',
}

// 请求拦截器
request.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        // 从 localStorage 获取 token
        const token = localStorage.getItem(STORAGE_KEYS.TOKEN)
        if (token && config.headers) {
            // 后端 AuthenticationInterceptor 使用 access-token 头
            config.headers['access-token'] = token
        }
        return config
    },
    (error) => {
        ElMessage.error('请求配置错误')
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    (response: AxiosResponse<BaseResponse<unknown>>) => {
        const data = response.data

        // 业务成功（code === 0）
        if (data.code === 0) {
            return response
        }

        // 业务错误 - 使用 ElMessage 优雅提示
        const errorMessage = data.message || ERROR_CODE_MAP[data.code] || '操作失败'

        // 根据错误类型选择不同的提示方式
        if (data.code === 40100 || data.code === 40101) {
            // 登录相关错误 - 使用警告提示
            ElMessage.warning(errorMessage)
            // 清除登录状态并跳转
            localStorage.removeItem(STORAGE_KEYS.TOKEN)
            localStorage.removeItem(STORAGE_KEYS.USER_INFO)
            router.push('/login')
        } else if (data.code >= 50000) {
            // 服务器错误 - 使用通知弹窗（更醒目）
            ElNotification({
                title: '服务异常',
                message: errorMessage,
                type: 'error',
                duration: 4000,
            })
        } else {
            // 其他业务错误 - 使用普通错误提示
            ElMessage.error(errorMessage)
        }

        return Promise.reject(new Error(errorMessage))
    },
    (error) => {
        // 网络或 HTTP 错误
        let errorMessage = '网络连接失败，请检查网络'

        if (error.response) {
            const status = error.response.status
            errorMessage = HTTP_STATUS_MAP[status] || `请求失败 (${status})`

            // 从响应体中尝试获取更详细的错误信息
            if (error.response.data?.message) {
                errorMessage = error.response.data.message
            }

            if (status === 401) {
                // 未授权，清除 token 并跳转登录页
                ElMessage.warning('登录已过期，请重新登录')
                localStorage.removeItem(STORAGE_KEYS.TOKEN)
                localStorage.removeItem(STORAGE_KEYS.USER_INFO)
                router.push('/login')
            } else if (status >= 500) {
                // 服务器错误使用通知弹窗
                ElNotification({
                    title: '服务器错误',
                    message: errorMessage,
                    type: 'error',
                    duration: 4000,
                })
            } else {
                ElMessage.error(errorMessage)
            }
        } else if (error.code === 'ECONNABORTED') {
            // 请求超时
            ElMessage.error('请求超时，请稍后重试')
            errorMessage = '请求超时'
        } else if (error.message === 'Network Error') {
            // 网络断开
            ElNotification({
                title: '网络错误',
                message: '无法连接到服务器，请检查网络连接',
                type: 'error',
                duration: 5000,
            })
        }

        return Promise.reject(new Error(errorMessage))
    }
)

export default request
