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

/**
 * 下载应用源码（ZIP 包）
 *
 * 由于后端直接返回二进制流而非 JSON，需要使用原生 fetch。
 * 同时手动读取 localStorage 中的 Token，以 access-token 头传递给后端，
 * 与 axios 拦截器保持一致。
 *
 * @param appId 应用ID
 */
export async function downloadAppCode(appId: string): Promise<void> {
    // 从 localStorage 读取 Token（与 axios 拦截器保持一致）
    const token = localStorage.getItem('auth_token')

    const headers: Record<string, string> = {}
    if (token) {
        headers['access-token'] = token
    }

    const response = await fetch(`/api/app/download?appId=${appId}`, {
        method: 'GET',
        headers,
    })

    if (!response.ok) {
        // 响应不成功时，解析后端返回的 JSON 错误信息并抛出
        // 注意：此时不能把响应体当 ZIP 处理，否则会保存一个损坏的文件
        const errBody = await response.json().catch(() => null)
        const message = errBody?.message || `下载失败 (${response.status})`
        throw new Error(message)
    }

    // 读取 Content-Disposition 头，提取文件名
    const disposition = response.headers.get('Content-Disposition') || ''
    let fileName = '项目源码.zip'
    const fileNameMatch = disposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
    if (fileNameMatch && fileNameMatch[1]) {
        // 解码 URL 编码的文件名（后端用 URLEncoder.encode 编码）
        fileName = decodeURIComponent(fileNameMatch[1].replace(/['"]/g, ''))
    }

    // 将响应体转为 Blob，创建临时 URL 触发下载
    const blob = await response.blob()
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    // 释放临时 URL，防止内存泄漏
    URL.revokeObjectURL(url)
}
