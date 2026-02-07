import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { AppVo } from '@/types/app'
import type { ChatMessage } from '@/types/common'
import * as appApi from '@/api/app'
import { chatToGenCode, getPreviewUrl } from '@/api/sse'

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

    // 加载应用信息
    async function loadApp(appId: string) {
        // 如果切换到不同的应用，重置消息列表
        if (currentApp.value?.id !== appId) {
            chatMessages.value = []
            cancelGeneration() // 取消正在进行的生成
        }

        const res = await appApi.getAppVoById(appId)
        currentApp.value = res.data
        updatePreviewUrl()
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
                        // 刷新预览
                        updatePreviewUrl()
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

    // 刷新预览
    function refreshPreview() {
        if (currentApp.value?.deployKey) {
            // 添加时间戳强制刷新
            previewUrl.value = getPreviewUrl(currentApp.value.deployKey) + `?t=${Date.now()}`
        } else if (currentApp.value) {
            previewUrl.value = getPreviewUrl(`${currentApp.value.codeGenType}_${currentApp.value.id}`) + `?t=${Date.now()}`
        }
    }

    return {
        // 状态
        currentApp,
        chatMessages,
        isGenerating,
        currentResponse,
        previewUrl,
        // 别名（为了兼容性）
        messages: chatMessages,
        isStreaming: isGenerating,
        // 方法
        loadApp,
        sendMessage,
        cancelGeneration,
        reset,
        refreshPreview,
    }
})
