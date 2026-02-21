import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { AppVo } from '@/types/app'
import type { ChatMessage, CursorInfo, ChatHistoryCursorQueryRequest } from '@/types/common'
import * as appApi from '@/api/app'
import { chatToGenCode, getPreviewUrl } from '@/api/sse'
import { listChatHistoryByPage } from '@/api/chatHistory'

export const useAppStore = defineStore('app', () => {
    // 当前应用
    const currentApp = ref<AppVo | null>(null)

    // 聊天消息列表
    const chatMessages = ref<ChatMessage[]>([])

    // 是否正在生成
    const isGenerating = ref(false)

    // 当前生成的 AI 回复
    const currentResponse = ref('')

    // 预览 URL
    const previewUrl = ref('')

    // SSE 取消函数
    let cancelSSE: (() => void) | null = null

    // 游标信息（用于加载更多）
    const cursorInfo = ref<CursorInfo | null>(null)

    // 是否还有更多历史消息
    const hasMoreHistory = ref(false)

    // 是否正在加载历史消息
    const isLoadingHistory = ref(false)

    // 加载应用信息
    async function loadApp(appId: string) {
        // 如果切换到不同的应用，重置所有状态
        if (currentApp.value?.id !== appId) {
            chatMessages.value = []
            cursorInfo.value = null
            hasMoreHistory.value = false
            cancelGeneration() // 取消正在进行的生成
        }

        const res = await appApi.getAppVoById(appId)
        currentApp.value = res.data
        updatePreviewUrl()

        // 加载历史消息
        await loadChatHistory(false)

        return res.data
    }

    // 更新预览 URL
    function updatePreviewUrl() {
        if (currentApp.value?.deployKey) {
            previewUrl.value = getPreviewUrl(currentApp.value.deployKey)
        } else if (currentApp.value) {
            // 如果还没有 deployKey，使用拼接的方式
            previewUrl.value = getPreviewUrl(`${currentApp.value.codeGenType}_${currentApp.value.id}`)
        }
    }

    // 添加用户消息
    function addUserMessage(content: string) {
        chatMessages.value.push({
            id: `user-${Date.now()}`,
            role: 'user',
            content,
            timestamp: Date.now(),
        })
    }

    // 发送消息并获取 AI 回复
    async function sendMessage(message: string): Promise<void> {
        console.log('[AppStore] sendMessage called, message:', message)
        console.log('[AppStore] currentApp:', currentApp.value)
        console.log('[AppStore] isGenerating:', isGenerating.value)

        if (!currentApp.value || isGenerating.value) {
            console.log('[AppStore] sendMessage 跳过: currentApp=', !!currentApp.value, 'isGenerating=', isGenerating.value)
            return
        }

        // 添加用户消息
        addUserMessage(message)

        // 开始生成
        isGenerating.value = true
        currentResponse.value = ''

        // 创建 AI 消息占位
        const aiMessageId = `ai-${Date.now()}`
        chatMessages.value.push({
            id: aiMessageId,
            role: 'assistant',
            content: '',
            timestamp: Date.now(),
        })

        console.log('[AppStore] 调用 SSE chatToGenCode, appId:', currentApp.value.id)

        return new Promise((resolve, reject) => {
            cancelSSE = chatToGenCode(
                currentApp.value!.id,
                message,
                {
                    onMessage: (content) => {
                        currentResponse.value += content
                        // 更新 AI 消息内容
                        const aiMsg = chatMessages.value.find(m => m.id === aiMessageId)
                        if (aiMsg) {
                            aiMsg.content = currentResponse.value
                        }
                    },
                    onComplete: () => {
                        isGenerating.value = false
                        currentResponse.value = ''
                        cancelSSE = null
                        console.log('[AppStore] SSE onComplete, codeGenType:', currentApp.value?.codeGenType)
                        // VUE_PROJECT 需要等待异步构建完成后再刷新预览
                        if (currentApp.value?.codeGenType === 'vue_project') {
                            // 清空预览 URL，让 iframe 隐藏，显示"等待生成"占位
                            console.log('[AppStore] VUE_PROJECT 检测到，清空预览并开始轮询构建状态')
                            previewUrl.value = ''
                            pollVueBuildReady()
                        } else {
                            // 非 VUE_PROJECT 直接刷新预览
                            updatePreviewUrl()
                        }
                        resolve()
                    },
                    onError: (error) => {
                        isGenerating.value = false
                        currentResponse.value = ''
                        cancelSSE = null
                        reject(error)
                    },
                }
            )
        })
    }

    // 取消生成
    function cancelGeneration() {
        if (cancelSSE) {
            cancelSSE()
            cancelSSE = null
            isGenerating.value = false
            currentResponse.value = ''
        }
    }

    // 重置状态
    function reset() {
        cancelGeneration()
        currentApp.value = null
        chatMessages.value = []
        previewUrl.value = ''
    }

    // 等待 VUE_PROJECT 异步构建完成后刷新预览
    function pollVueBuildReady() {
        if (!currentApp.value) return

        const appId = currentApp.value.id
        let attempts = 0
        const maxAttempts = 40 // 最多轮询 40 次（约 2 分钟）

        const timer = setInterval(async () => {
            attempts++
            try {
                const token = localStorage.getItem('auth_token') || ''
                const response = await fetch(`/api/app/build/status?appId=${appId}`, {
                    headers: { 'access-token': token }
                })
                if (response.ok) {
                    const result = await response.json()
                    console.log(`[AppStore] 轮询构建状态 #${attempts}: data=${result.data}`)
                    if (result.data === 'done') {
                        clearInterval(timer)
                        console.log('[AppStore] Vue 项目构建完成，刷新预览')
                        refreshPreview()
                        return
                    }
                    if (result.data === 'failed') {
                        clearInterval(timer)
                        console.error('[AppStore] Vue 项目构建失败')
                        return
                    }
                    // 'building' 或 null → 继续轮询
                }
            } catch {
                // 忽略网络错误，继续轮询
            }
            if (attempts >= maxAttempts) {
                clearInterval(timer)
                console.warn('[AppStore] Vue 项目构建超时，停止轮询')
            }
        }, 3000)
    }

    // 刷新预览
    function refreshPreview() {
        if (currentApp.value?.deployKey) {
            // 添加时间戳强制刷新
            previewUrl.value = getPreviewUrl(currentApp.value.deployKey) + `?t=${Date.now()}`
        } else if (currentApp.value) {
            previewUrl.value = getPreviewUrl(`${currentApp.value.codeGenType}_${currentApp.value.id}`) + `?t=${Date.now()}`
        }
    }

    // 加载历史消息
    async function loadChatHistory(isLoadMore: boolean = false): Promise<void> {
        if (!currentApp.value || isLoadingHistory.value) return

        isLoadingHistory.value = true

        try {
            const params: ChatHistoryCursorQueryRequest = {
                appId: currentApp.value.id,
                pageSize: 10
            }

            // 如果是加载更多，携带游标信息
            if (isLoadMore && cursorInfo.value) {
                params.lastTime = cursorInfo.value.lastTime
                params.lastId = cursorInfo.value.lastId
            }

            const res = await listChatHistoryByPage(params)
            const { records, hasMore, nextCursor } = res.data

            // 后端返回的是降序（最新在前），需要反转为升序（最旧在前，最新在后）
            // 这样最新消息显示在底部，符合聊天习惯
            const historyMessages: ChatMessage[] = records.reverse().map(record => ({
                id: `history-${record.id}`,
                role: record.messageType === 'user' ? 'user' : 'assistant',
                content: record.message,
                timestamp: new Date(record.createTime).getTime()
            }))

            // 如果是加载更多，将更旧的消息插入到列表开头
            // 如果是首次加载，直接设置
            if (isLoadMore) {
                chatMessages.value = [...historyMessages, ...chatMessages.value]
            } else {
                chatMessages.value = historyMessages
            }

            // 更新游标和hasMore状态
            hasMoreHistory.value = hasMore
            cursorInfo.value = nextCursor || null
        } catch (error) {
            console.error('[AppStore] 加载历史消息失败:', error)
            throw error
        } finally {
            isLoadingHistory.value = false
        }
    }

    return {
        // 状态
        currentApp,
        chatMessages,
        isGenerating,
        currentResponse,
        previewUrl,
        cursorInfo,
        hasMoreHistory,
        isLoadingHistory,
        // 别名（为了兼容性）
        messages: chatMessages,
        isStreaming: isGenerating,
        // 方法
        loadApp,
        sendMessage,
        cancelGeneration,
        reset,
        refreshPreview,
        loadChatHistory,
    }
})
