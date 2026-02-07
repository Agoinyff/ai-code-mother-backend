// 用户视图对象
export interface UserVo {
    userAccount: string
    userName: string
    userAvatar: string
    userProfile: string
    userRole: 'user' | 'admin'
    shareCode: string
    inviteUser: number | null
    vipExpireTime: string | null
    vipCode: string | null
    vipNumber: number | null
    createTime: string
    updateTime: string
}

// 登录响应
export interface LoginVo {
    userVo: UserVo
    token: string
}

// 登录请求
export interface UserLoginRequest {
    userAccount: string
    userPassword: string
}

// 注册请求
export interface UserRegisterRequest {
    userAccount: string
    userPassword: string
    checkPassword: string
}

// 用户查询请求
export interface UserQueryRequest {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    userAccount?: string
    userName?: string
}

// 用户添加请求
export interface UserAddRequest {
    userAccount: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
}

// 用户更新请求
export interface UserUpdateRequest {
    id: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
}
