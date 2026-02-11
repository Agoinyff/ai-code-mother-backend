import request from '@/utils/request'
import type { BaseResponse, PageResult } from '@/types/common'
import type { ChatHistoryCursorQueryRequest, CursorPageVo, ChatHistoryVo } from '@/types/common'

/**
 * 分页查询指定应用的对话历史（游标分页）
 */
export function listChatHistoryByPage(
    data: ChatHistoryCursorQueryRequest
): Promise<BaseResponse<CursorPageVo<ChatHistoryVo>>> {
    return request.post('/chatHistory/list/page', data).then(res => res.data)
}

// ============ 管理员接口 ============

export interface ChatHistoryAdminQueryRequest {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    appId?: number
    userId?: number
    messageType?: string
    message?: string
}

/**
 * 【管理员】分页查询所有对话历史
 */
export function adminListChatHistory(
    data: ChatHistoryAdminQueryRequest
): Promise<BaseResponse<PageResult<ChatHistoryVo>>> {
    return request.post('/chatHistory/admin/list/page', data).then(res => res.data)
}
