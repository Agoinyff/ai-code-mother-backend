import { API_CONFIG, STORAGE_KEYS } from '@/config/api'

export interface SSECallbacks {
    onMessage: (content: string) => void
    onComplete: () => void
    onError: (error: Error) => void
}

/**
 * 创建 SSE 连接进行代码生成对话
 * @param appId 应用 ID
 * @param userMessage 用户消息
 * @param callbacks 回调函数
 * @returns 关闭连接的函数
 */
export function chatToGenCode(
    appId: string,
    userMessage: string,
    callbacks: SSECallbacks
): () => void {
    const token = localStorage.getItem(STORAGE_KEYS.TOKEN)
    const url = new URL(`${window.location.origin}${API_CONFIG.BASE_URL}/app/chat/gen/code`)
    url.searchParams.set('appId', String(appId))
    url.searchParams.set('userMessage', userMessage)

    // 使用 fetch + ReadableStream 处理 SSE（因为 EventSource 不支持自定义 headers）
    const controller = new AbortController()

    fetch(url.toString(), {
        method: 'GET',
        headers: {
            'access-token': token || '',
            'Accept': 'text/event-stream',
        },
        signal: controller.signal,
    })
        .then(async (response) => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`)
            }

            const reader = response.body?.getReader()
            if (!reader) {
                throw new Error('No response body')
            }

            const decoder = new TextDecoder()
            let buffer = ''

            while (true) {
                const { done, value } = await reader.read()

                if (done) {
                    callbacks.onComplete()
                    break
                }

                buffer += decoder.decode(value, { stream: true })

                // 按行分割处理 SSE 数据
                const lines = buffer.split('\n')
                buffer = lines.pop() || '' // 保留最后一行（可能不完整）

                for (const line of lines) {
                    if (line.startsWith('event:done')) {
                        // 收到完成事件
                        callbacks.onComplete()
                        return
                    }

                    if (line.startsWith('data:')) {
                        const jsonStr = line.slice(5).trim()
                        if (jsonStr) {
                            try {
                                const data = JSON.parse(jsonStr)
                                if (data.d) {
                                    callbacks.onMessage(data.d)
                                }
                            } catch (e) {
                                // 忽略解析错误
                            }
                        }
                    }
                }
            }
        })
        .catch((error) => {
            if (error.name !== 'AbortError') {
                callbacks.onError(error)
            }
        })

    // 返回取消函数
    return () => {
        controller.abort()
    }
}

/**
 * 获取网站预览 URL
 * 使用相对路径，开发环境下通过 Vite proxy 代理到后端，生产环境下前后端同域
 * @param deployKey 部署 key（格式：codeGenType_appId）
 */
export function getPreviewUrl(deployKey: string): string {
    return `/api/static/${deployKey}/`
}
