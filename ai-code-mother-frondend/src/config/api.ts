// API 配置
export const API_CONFIG = {
  // 后端 API 基础路径
  BASE_URL: '/api',
  
  // 静态资源预览路径
  STATIC_BASE_URL: '/api/static',
  
  // 请求超时时间（毫秒）
  TIMEOUT: 30000,
}

// 本地存储键名
export const STORAGE_KEYS = {
  TOKEN: 'auth_token',
  USER_INFO: 'user_info',
}

// 代码生成类型选项
export const CODE_GEN_TYPES = [
  { value: 'html', label: 'HTML' },
  { value: 'react', label: 'React' },
  { value: 'vue', label: 'Vue' },
] as const

export type CodeGenType = typeof CODE_GEN_TYPES[number]['value']
