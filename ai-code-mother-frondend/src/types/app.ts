// 应用视图对象
export interface AppVo {
    id: string  // 使用 string 避免大整数精度丢失
    appName: string
    cover: string
    initPrompt: string
    codeGenType: string
    deployKey: string | null
    deployedTime: string | null
    priority: number
    userId: string
    createTime: string
    updateTime: string
}

// 创建应用请求
export interface AppAddRequest {
    appName?: string
    cover?: string
    initPrompt: string
    codeGenType: string
}

// 更新应用请求
export interface AppUpdateRequest {
    id: string
    appName: string
}

// 应用查询请求
export interface AppQueryRequest {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    appName?: string
}

// 部署应用请求
export interface AppDeployRequest {
    appId: string
}

// 管理员更新应用请求
export interface AppAdminUpdateRequest {
    id: string
    appName?: string
    cover?: string
    priority?: number
}

// 管理员查询应用请求
export interface AppAdminQueryRequest {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    appName?: string
    userId?: string
    codeGenType?: string
}
