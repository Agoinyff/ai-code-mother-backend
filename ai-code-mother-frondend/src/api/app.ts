import request from '@/utils/request'
import type { BaseResponse, PageResult } from '@/types/common'
import type {
    AppVo,
    AppAddRequest,
    AppUpdateRequest,
    AppQueryRequest,
    AppDeployRequest,
    AppAdminUpdateRequest,
    AppAdminQueryRequest
} from '@/types/app'

// 创建应用（返回应用ID字符串）
export function createApp(data: AppAddRequest): Promise<BaseResponse<string>> {
    return request.post('/app/add', data).then(res => res.data)
}

// 获取应用详情 VO
export function getAppVoById(id: string): Promise<BaseResponse<AppVo>> {
    return request.get('/app/get/vo', { params: { id } }).then(res => res.data)
}

// 分页查询我的应用列表
export function listMyApps(data: AppQueryRequest): Promise<BaseResponse<PageResult<AppVo>>> {
    return request.post('/app/list/my/page/vo', data).then(res => res.data)
}

// 分页查询精选应用列表
export function listFeaturedApps(data: AppQueryRequest): Promise<BaseResponse<PageResult<AppVo>>> {
    return request.post('/app/list/featured/page/vo', data).then(res => res.data)
}

// 更新自己的应用
export function updateMyApp(data: AppUpdateRequest): Promise<BaseResponse<boolean>> {
    return request.post('/app/update/my', data).then(res => res.data)
}

// 删除自己的应用
export function deleteMyApp(id: string): Promise<BaseResponse<boolean>> {
    return request.post('/app/delete/my', null, { params: { id } }).then(res => res.data)
}

// 部署应用
export function deployApp(data: AppDeployRequest): Promise<BaseResponse<string>> {
    return request.post('/app/deploy', data).then(res => res.data)
}

// ============ 管理员接口 ============

// 管理员删除应用
export function adminDeleteApp(id: string): Promise<BaseResponse<boolean>> {
    return request.post('/app/admin/delete', null, { params: { id } }).then(res => res.data)
}

// 管理员更新应用
export function adminUpdateApp(data: AppAdminUpdateRequest): Promise<BaseResponse<boolean>> {
    return request.post('/app/admin/update', data).then(res => res.data)
}

// 管理员分页查询应用
export function adminListApps(data: AppAdminQueryRequest): Promise<BaseResponse<PageResult<AppVo>>> {
    return request.post('/app/admin/list/page/vo', data).then(res => res.data)
}

// 管理员获取应用详情
export function adminGetAppById(id: string): Promise<BaseResponse<AppVo>> {
    return request.get('/app/admin/get', { params: { id } }).then(res => res.data)
}
