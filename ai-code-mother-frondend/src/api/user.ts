import request from '@/utils/request'
import type { BaseResponse } from '@/types/common'
import type {
    UserVo,
    LoginVo,
    UserLoginRequest,
    UserRegisterRequest,
    UserQueryRequest,
    UserAddRequest,
    UserUpdateRequest
} from '@/types/user'
import type { PageResult } from '@/types/common'

// 用户注册
export function register(data: UserRegisterRequest): Promise<BaseResponse<number>> {
    return request.post('/user/register', data).then(res => res.data)
}

// 用户登录
export function login(data: UserLoginRequest): Promise<BaseResponse<LoginVo>> {
    return request.post('/user/login', data).then(res => res.data)
}

// 用户登出
export function logout(): Promise<BaseResponse<boolean>> {
    return request.post('/user/logout').then(res => res.data)
}

// 获取当前登录用户信息
export function getLoginUser(): Promise<BaseResponse<UserVo>> {
    return request.get('/user/get/login').then(res => res.data)
}

// 根据 ID 获取用户 VO
export function getUserVoById(id: number): Promise<BaseResponse<UserVo>> {
    return request.get('/user/get/vo', { params: { id } }).then(res => res.data)
}

// ============ 管理员接口 ============

// 创建用户
export function addUser(data: UserAddRequest): Promise<BaseResponse<number>> {
    return request.post('/user/add', data).then(res => res.data)
}

// 删除用户
export function deleteUser(id: number): Promise<BaseResponse<boolean>> {
    return request.post('/user/delete', null, { params: { id } }).then(res => res.data)
}

// 更新用户
export function updateUser(data: UserUpdateRequest): Promise<BaseResponse<boolean>> {
    return request.post('/user/update', data).then(res => res.data)
}

// 分页获取用户列表
export function listUserVoByPage(data: UserQueryRequest): Promise<BaseResponse<PageResult<UserVo>>> {
    return request.post('/user/list/page/vo', data).then(res => res.data)
}

// 获取用户详情（未脱敏）
export function getUserById(id: number): Promise<BaseResponse<UserVo>> {
    return request.get('/user/get', { params: { id } }).then(res => res.data)
}
