import request from '@/utils/request'
import type { BaseResponse } from '@/types/common'

/** 部署历史记录类型 */
export interface DeployHistory {
    id: string
    appId: string
    version: number
    imageTag: string
    containerId: string
    containerPort: number
    status: string  // RUNNING | STOPPED | FAILED
    deployUrl: string
    userId: string
    createTime: string
    updateTime: string
}

// ============ Docker 预览接口 ============

/** 启动 Docker 预览容器 */
export function startPreview(appId: string): Promise<BaseResponse<string>> {
    return request.post('/app/preview/start', { appId }).then(res => res.data)
}

/** 停止 Docker 预览容器 */
export function stopPreview(appId: string): Promise<BaseResponse<boolean>> {
    return request.post('/app/preview/stop', { appId }).then(res => res.data)
}

// ============ 部署版本管理接口 ============

/** 查询部署版本历史 */
export function getDeployVersions(appId: string): Promise<BaseResponse<DeployHistory[]>> {
    return request.get('/app/deploy/versions', { params: { appId } }).then(res => res.data)
}

/** 回滚到指定部署版本 */
export function rollbackDeploy(appId: string, version: number): Promise<BaseResponse<string>> {
    return request.post('/app/deploy/rollback', null, { params: { appId, version } }).then(res => res.data)
}

/** 停止已部署的容器（下线应用） */
export function stopDeploy(appId: string): Promise<BaseResponse<boolean>> {
    return request.post('/app/deploy/stop', { appId }).then(res => res.data)
}

