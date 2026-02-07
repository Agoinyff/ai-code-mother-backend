// 后端统一响应格式
export interface BaseResponse<T> {
    code: number
    message: string
    data: T
}

// 分页请求参数
export interface PageRequest {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: 'ascend' | 'descend'
}

// 分页响应结果（MyBatis-Plus Page 格式）
export interface PageResult<T> {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
}

// 聊天消息类型
export interface ChatMessage {
    id: string
    role: 'user' | 'assistant'
    content: string
    timestamp: number
}
