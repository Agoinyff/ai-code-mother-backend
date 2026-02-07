<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { deployApp } from '@/api/app'
import { getPreviewUrl } from '@/api/sse'
import { ElMessage, ElDialog, ElInput, ElButton, ElDropdown, ElDropdownMenu, ElDropdownItem } from 'element-plus'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const appId = computed(() => String(route.params.appId))
const chatMessages = computed(() => appStore.chatMessages)
const currentApp = computed(() => appStore.currentApp)
const isStreaming = computed(() => appStore.isGenerating)

// 滚动到底部
const chatListRef = ref<HTMLElement>()
function scrollToBottom() {
    nextTick(() => {
        if (chatListRef.value) {
            chatListRef.value.scrollTop = chatListRef.value.scrollHeight
        }
    })
}
watch(chatMessages, scrollToBottom, { deep: true })

// 消息输入
const userMessage = ref('')
async function sendMessage() {
    if (!userMessage.value.trim() || isStreaming.value) return
    const msg = userMessage.value.trim()
    userMessage.value = ''
    await appStore.sendMessage(msg)
    scrollToBottom()
}

// 部署
const showDeployDialog = ref(false)
const deployUrl = ref('')
const isDeploying = ref(false)

async function handleDeploy() {
    if (!currentApp.value) return
    isDeploying.value = true
    try {
        const res = await deployApp({ appId: currentApp.value.id })
        deployUrl.value = res.data
        showDeployDialog.value = true
        // 刷新应用信息获取最新 deployKey
        await appStore.loadApp(appId.value)
    } catch (error: any) {
        ElMessage.error(error.message || '部署失败')
    } finally {
        isDeploying.value = false
    }
}

function copyDeployUrl() {
    navigator.clipboard.writeText(deployUrl.value)
    ElMessage.success('链接已复制')
}

function openDeployUrl() {
    window.open(deployUrl.value, '_blank')
}

// 预览 URL（支持新创建的应用和已部署的应用）
const previewUrl = computed(() => {
    if (currentApp.value?.deployKey) {
        return getPreviewUrl(currentApp.value.deployKey)
    }
    // 新创建的应用使用 codeGenType_appId 格式
    if (currentApp.value) {
        return getPreviewUrl(`${currentApp.value.codeGenType}_${currentApp.value.id}`)
    }
    return null
})

// 格式化 AI 消息：直接显示文本内容
function formatAIMessage(content: string): string {
    if (!content) return ''
    // 简单转义 HTML 并保留换行
    return content
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/\n/g, '<br>')
}

// 加载应用
onMounted(async () => {
    console.log('[ChatPage] onMounted, appId:', appId.value)
    try {
        const app = await appStore.loadApp(appId.value)
        console.log('[ChatPage] 应用加载成功:', app)
        console.log('[ChatPage] initPrompt:', app?.initPrompt)
        console.log('[ChatPage] chatMessages.length:', chatMessages.value.length)
        
        // 如果有初始 prompt 且没有消息历史，自动发送
        if (app?.initPrompt && chatMessages.value.length === 0) {
            console.log('[ChatPage] 触发自动发送 initPrompt')
            await appStore.sendMessage(app.initPrompt)
        }
        scrollToBottom()
    } catch (error) {
        console.error('[ChatPage] 加载应用失败:', error)
        ElMessage.error('加载应用失败')
    }
})

onBeforeUnmount(() => {
    appStore.cancelGeneration()
})
</script>

<template>
    <div class="chat-page">
        <!-- 顶部栏 -->
        <header class="chat-header">
            <div class="header-left">
                <ElDropdown trigger="click">
                    <div class="app-selector">
                        <span class="app-icon">🐱</span>
                        <span class="app-name">{{ currentApp?.appName || '应用名称' }}</span>
                        <el-icon><ArrowDown /></el-icon>
                    </div>
                    <template #dropdown>
                        <ElDropdownMenu>
                            <ElDropdownItem @click="router.push('/my-apps')">
                                我的其他应用
                            </ElDropdownItem>
                        </ElDropdownMenu>
                    </template>
                </ElDropdown>
            </div>
            <div class="header-center">
                <span class="preview-label">生成后的网页展示</span>
            </div>
            <div class="header-right">
                <ElButton
                    type="danger"
                    :loading="isDeploying"
                    :disabled="!previewUrl || isStreaming"
                    @click="handleDeploy"
                >
                    <el-icon><Upload /></el-icon>
                    部署
                </ElButton>
            </div>
        </header>

        <!-- 主内容区 -->
        <div class="chat-body">
            <!-- 左侧对话区 -->
            <div class="chat-panel">
                <!-- 对话列表 -->
                <div ref="chatListRef" class="chat-list">
                    <div
                        v-for="(msg, index) in chatMessages"
                        :key="index"
                        class="message-item"
                        :class="msg.role"
                    >
                        <div v-if="msg.role === 'user'" class="message-label">用户消息</div>
                        <div v-else class="message-label">AI 回复</div>
                        <div class="message-content">
                            <template v-if="msg.role === 'assistant'">
                                <div class="message-text" v-html="formatAIMessage(msg.content)"></div>
                            </template>
                            <template v-else>
                                <div class="message-text">{{ msg.content }}</div>
                            </template>
                        </div>
                    </div>
                    <div v-if="isStreaming" class="streaming-indicator">
                        <el-icon class="is-loading"><Loading /></el-icon>
                        正在生成...
                    </div>
                </div>

                <!-- 输入框 -->
                <div class="chat-input">
                    <ElInput
                        v-model="userMessage"
                        type="textarea"
                        :rows="3"
                        resize="none"
                        placeholder="描述越详细，页面越具体，可进一步完善生成效果"
                        :disabled="isStreaming"
                        @keydown.enter.prevent="sendMessage"
                    />
                    <div class="input-actions">
                        <div class="action-left">
                            <el-button text size="small">
                                <el-icon><Upload /></el-icon>
                                上传
                            </el-button>
                            <el-button text size="small">
                                <el-icon><Edit /></el-icon>
                                编辑
                            </el-button>
                            <el-button text size="small">
                                <el-icon><MagicStick /></el-icon>
                                优化
                            </el-button>
                        </div>
                        <el-button
                            type="primary"
                            circle
                            :loading="isStreaming"
                            :disabled="!userMessage.trim()"
                            @click="sendMessage"
                        >
                            <el-icon v-if="!isStreaming"><Top /></el-icon>
                        </el-button>
                    </div>
                </div>
            </div>

            <!-- 右侧预览区 -->
            <div class="preview-panel">
                <div v-if="previewUrl" class="preview-container">
                    <iframe
                        :src="previewUrl"
                        class="preview-iframe"
                        frameborder="0"
                        sandbox="allow-scripts allow-same-origin"
                    ></iframe>
                </div>
                <div v-else class="preview-empty">
                    <el-icon size="64" color="var(--text-muted)"><Monitor /></el-icon>
                    <p>AI 正在生成中，请稍候...</p>
                </div>
            </div>
        </div>

        <!-- 部署成功弹窗 -->
        <ElDialog v-model="showDeployDialog" title="🎉 部署成功" width="480px" center>
            <div class="deploy-success">
                <p>您的应用已成功部署，访问链接：</p>
                <div class="deploy-url">
                    <code>{{ deployUrl }}</code>
                    <el-button type="primary" size="small" @click="copyDeployUrl">复制</el-button>
                </div>
            </div>
            <template #footer>
                <el-button @click="showDeployDialog = false">关闭</el-button>
                <el-button type="primary" @click="openDeployUrl">
                    访问网站
                </el-button>
            </template>
        </ElDialog>
    </div>
</template>

<script lang="ts">
// AI 消息格式化（支持 Markdown 风格）
function formatAIMessage(content: string): string {
    if (!content) return ''
    // 简单处理代码块和文件路径
    return content
        .replace(/`([^`]+)`/g, '<code>$1</code>')
        .replace(/\n/g, '<br>')
}
</script>

<style scoped>
.chat-page {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background: #f8f9fa;
}

/* Header */
.chat-header {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 100;
    height: 56px;
    background: #fff;
    border-bottom: 1px solid var(--border-light);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
}

.header-left,
.header-right {
    flex: 1;
}

.header-right {
    display: flex;
    justify-content: flex-end;
}

.app-selector {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background: #fef5f5;
    border-radius: 8px;
    cursor: pointer;
    font-weight: 500;
}

.app-icon {
    font-size: 20px;
}

.app-name {
    max-width: 200px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.preview-label {
    font-size: 14px;
    color: var(--text-secondary);
}

/* Body */
.chat-body {
    flex: 1;
    display: flex;
    margin-top: 56px;
    height: calc(100vh - 56px);
}

/* Chat Panel */
.chat-panel {
    width: 420px;
    min-width: 360px;
    display: flex;
    flex-direction: column;
    border-right: 1px solid var(--border-light);
    background: #fff;
}

.chat-list {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
}

.message-item {
    margin-bottom: 20px;
}

.message-label {
    font-size: 12px;
    color: var(--text-muted);
    margin-bottom: 8px;
}

.message-item.user .message-label {
    color: #ff6b6b;
}

.message-content {
    background: #f8f9fa;
    border-radius: 12px;
    padding: 12px 16px;
}

.message-item.user .message-content {
    background: #fef5f5;
    border: 1px solid #ffe0e0;
}

.message-text {
    font-size: 14px;
    line-height: 1.6;
    color: var(--text-primary);
}

.message-text code {
    background: rgba(0, 0, 0, 0.06);
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'Fira Code', monospace;
    font-size: 13px;
}

.streaming-indicator {
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--primary-color);
    font-size: 14px;
    padding: 12px;
}

/* Input */
.chat-input {
    border-top: 1px solid var(--border-light);
    padding: 16px;
    background: #fff;
}

.chat-input :deep(.el-textarea__inner) {
    border: 1px solid var(--border-color);
    border-radius: 12px;
    padding: 12px 16px;
    resize: none;
}

.chat-input :deep(.el-textarea__inner):focus {
    border-color: #ff6b6b;
    box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.1);
}

.input-actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 12px;
}

.action-left {
    display: flex;
    gap: 4px;
}

/* Preview Panel */
.preview-panel {
    flex: 1;
    background: #f0f2f5;
    display: flex;
    align-items: center;
    justify-content: center;
}

.preview-container {
    width: 90%;
    height: 90%;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.1);
    overflow: hidden;
}

.preview-iframe {
    width: 100%;
    height: 100%;
}

.preview-empty {
    text-align: center;
    color: var(--text-muted);
}

.preview-empty p {
    margin-top: 16px;
    font-size: 15px;
}

/* Deploy Dialog */
.deploy-success {
    text-align: center;
}

.deploy-success p {
    margin-bottom: 16px;
    color: var(--text-secondary);
}

.deploy-url {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: var(--bg-secondary);
    border-radius: 8px;
}

.deploy-url code {
    flex: 1;
    font-size: 13px;
    word-break: break-all;
}

@media (max-width: 992px) {
    .chat-panel {
        width: 320px;
        min-width: 280px;
    }
}

@media (max-width: 768px) {
    .chat-body {
        flex-direction: column;
    }

    .chat-panel {
        width: 100%;
        height: 50%;
    }

    .preview-panel {
        height: 50%;
    }
}
</style>
