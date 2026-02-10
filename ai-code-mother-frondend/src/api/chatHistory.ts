import request from '@/utils/request'
import type { BaseResponse } from '@/types/common'
import type { ChatHistoryCursorQueryRequest, CursorPageVo, ChatHistoryVo } from '@/types/common'

/**
 * 分页查询指定应用的对话历史（游标分页）
 */
export function listChatHistoryByPage(
    data: ChatHistoryCursorQueryRequest
): Promise<BaseResponse<CursorPageVo<ChatHistoryVo>>> {
    return request.post('/chatHistory/list/page', data).then(res => res.data)
}
