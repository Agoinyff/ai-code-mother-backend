<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { deployApp } from '@/api/app'
import { getPreviewUrl } from '@/api/sse'
import { ElMessage, ElDialog, ElButton, ElInput } from 'element-plus'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const appId = computed(() => String(route.params.appId))
const chatMessages = computed(() => appStore.chatMessages)
const currentApp = computed(() => appStore.currentApp)
const isGenerating = computed(() => appStore.isGenerating)

// 是否显示预览面板（生成完成后显示）
const showPreview = ref(false)

// 预览刷新 key（用于强制刷新 iframe）
const previewKey = ref(0)
const iframeRef = ref<HTMLIFrameElement>()

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

// 监听生成状态变化，生成完成后显示预览
watch(isGenerating, (newVal, oldVal) => {
    if (oldVal === true && newVal === false) {
        // 生成刚完成，延迟显示预览
        setTimeout(() => {
            showPreview.value = true
        }, 500)
    }
})

// 消息输入
const userMessage = ref('')
async function sendMessage() {
    if (!userMessage.value.trim() || isGenerating.value) return
    const msg = userMessage.value.trim()
    userMessage.value = ''
    // 开始生成时隐藏预览（可选，如果要保留之前预览可以去掉）
    // showPreview.value = false
    await appStore.sendMessage(msg)
    scrollToBottom()
}

// 部署相关
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

// 预览 URL（使用 StaticResourceController 接口）
const previewUrl = computed(() => {
    if (currentApp.value?.deployKey) {
        return getPreviewUrl(currentApp.value.deployKey)
    }
    if (currentApp.value) {
        return getPreviewUrl(`${currentApp.value.codeGenType}_${currentApp.value.id}`)
    }
    return null
})

// 格式化 AI 消息
function formatAIMessage(content: string): string {
    if (!content) return ''
    return content
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/\n/g, '<br>')
}

// 刷新预览（通过更新 key 强制重载 iframe）
function refreshPreview() {
    previewKey.value++
    // 或直接刷新 iframe
    if (iframeRef.value) {
        iframeRef.value.src = iframeRef.value.src
    }
}

// 新窗口打开预览
function openPreviewInNewTab() {
    if (previewUrl.value) {
        window.open(previewUrl.value, '_blank')
    }
}

// 加载应用
onMounted(async () => {
    try {
        const app = await appStore.loadApp(appId.value)
        if (app?.initPrompt && chatMessages.value.length === 0) {
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

// 返回首页
function goHome() {
    router.push('/')
}
</script>

<template>
    <div class="chat-page">
        <!-- 顶部栏 -->
        <header class="chat-header">
            <div class="header-left">
                <el-button text @click="goHome">
                    <el-icon><ArrowLeft /></el-icon>
                    返回
                </el-button>
                <span class="app-name">{{ currentApp?.appName || '应用名称' }}</span>
            </div>
            <div class="header-right">
                <el-button
                    v-if="showPreview && previewUrl"
                    type="primary"
                    :loading="isDeploying"
                    @click="handleDeploy"
                    class="deploy-btn"
                >
                    <el-icon><Upload /></el-icon>
                    部署上线
                </el-button>
            </div>
        </header>

        <!-- 主内容区 - 始终保持居中 -->
        <div class="chat-body">
            <div class="panels-wrapper" :class="{ 'with-preview': showPreview && previewUrl }">
                <!-- 聊天区域 -->
                <div class="chat-panel">
                    <!-- 对话列表 -->
                    <div ref="chatListRef" class="chat-list">
                        <div
                            v-for="(msg, index) in chatMessages"
                            :key="index"
                            class="message-item"
                            :class="msg.role"
                        >
                            <div class="message-header">
                                <span class="message-role">{{ msg.role === 'user' ? '👤 用户' : '🤖 AI' }}</span>
                            </div>
                            <div class="message-content">
                                <div v-if="msg.role === 'assistant'" class="message-text" v-html="formatAIMessage(msg.content)"></div>
                                <div v-else class="message-text">{{ msg.content }}</div>
                            </div>
                        </div>
                        
                        <!-- 流式生成指示器 -->
                        <div v-if="isGenerating" class="streaming-indicator">
                            <div class="typing-dots">
                                <span></span>
                                <span></span>
                                <span></span>
                            </div>
                            <span class="typing-text">AI 正在生成中...</span>
                        </div>
                        
                        <!-- 无消息时的占位 -->
                        <div v-if="chatMessages.length === 0 && !isGenerating" class="empty-chat">
                            <el-icon :size="48" color="#ddd"><ChatDotRound /></el-icon>
                            <p>开始对话，让 AI 帮您生成网页</p>
                        </div>
                    </div>

                    <!-- 输入框 -->
                    <div class="chat-input">
                        <ElInput
                            v-model="userMessage"
                            type="textarea"
                            :rows="3"
                            resize="none"
                            placeholder="继续描述您的需求，AI 会帮您完善..."
                            :disabled="isGenerating"
                            @keydown.enter.exact.prevent="sendMessage"
                        />
                        <div class="input-actions">
                            <el-button
                                type="primary"
                                :loading="isGenerating"
                                :disabled="!userMessage.trim()"
                                @click="sendMessage"
                            >
                                <el-icon v-if="!isGenerating"><Promotion /></el-icon>
                                发送
                            </el-button>
                        </div>
                    </div>
                </div>

                <!-- 预览区域（动画显示） -->
                <transition name="preview-fade">
                    <div v-if="showPreview && previewUrl" class="preview-panel">
                        <div class="preview-header">
                            <span class="preview-title">🖥️ 实时预览</span>
                            <div class="preview-actions">
                                <el-button 
                                    type="primary" 
                                    size="small" 
                                    :loading="isDeploying"
                                    @click="handleDeploy"
                                >
                                    <el-icon v-if="!isDeploying"><Upload /></el-icon>
                                    {{ isDeploying ? '部署中...' : '部署应用' }}
                                </el-button>
                                <el-button text size="small" @click="refreshPreview">
                                    <el-icon><Refresh /></el-icon>
                                    刷新
                                </el-button>
                                <el-button text size="small" @click="openPreviewInNewTab">
                                    <el-icon><Link /></el-icon>
                                    新窗口
                                </el-button>
                            </div>
                        </div>
                        <div class="preview-container">
                            <iframe
                                ref="iframeRef"
                                :key="previewKey"
                                :src="previewUrl"
                                class="preview-iframe"
                                frameborder="0"
                                sandbox="allow-scripts allow-same-origin allow-forms"
                            ></iframe>
                        </div>
                    </div>
                </transition>
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
                <el-button type="primary" @click="openDeployUrl">立即访问</el-button>
            </template>
        </ElDialog>
    </div>
</template>

<style scoped>
.chat-page {
    height: 100vh;
    display: flex;
    flex-direction: column;
    background: linear-gradient(145deg, #f8fafc 0%, #e2e8f0 100%);
}

/* Header */
.chat-header {
    height: 60px;
    padding: 0 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
    flex-shrink: 0;
    z-index: 100;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 16px;
}

.app-name {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
}

.deploy-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
}

.deploy-btn:hover {
    background: linear-gradient(135deg, #5a6fd6 0%, #6a4190 100%);
}

/* Main Body - 保持内容居中 */
.chat-body {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: flex-start;
    overflow: hidden;
    padding: 24px;
}

/* Panels Wrapper - 控制整体居中 */
.panels-wrapper {
    display: flex;
    justify-content: center;
    gap: 24px;
    width: 100%;
    max-width: 800px;
    height: 100%;
    transition: max-width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.panels-wrapper.with-preview {
    max-width: 1400px;
}

/* Chat Panel */
.chat-panel {
    flex: 1;
    min-width: 400px;
    max-width: 600px;
    display: flex;
    flex-direction: column;
    background: #fff;
    border-radius: 20px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
    overflow: hidden;
    position: relative;
}

/* Generating Overlay */
.generating-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 76px; /* 留出输入框空间 */
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(4px);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 10;
}

.generating-content {
    text-align: center;
}

.spinner {
    width: 48px;
    height: 48px;
    margin: 0 auto 20px;
    border: 3px solid #e0e0e0;
    border-top-color: #667eea;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    to { transform: rotate(360deg); }
}

.generating-content h3 {
    font-size: 18px;
    color: #333;
    margin-bottom: 8px;
}

.generating-content p {
    font-size: 14px;
    color: #888;
}

/* Chat List */
.chat-list {
    flex: 1;
    overflow-y: auto;
    padding: 24px;
}

.empty-chat {
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #999;
}

.empty-chat p {
    margin-top: 12px;
    font-size: 14px;
}

.message-item {
    margin-bottom: 20px;
    animation: fadeInUp 0.3s ease;
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.message-header {
    margin-bottom: 8px;
}

.message-role {
    font-size: 13px;
    font-weight: 500;
    color: #666;
}

.message-content {
    background: #f7f8fa;
    border-radius: 16px;
    padding: 16px 20px;
    transition: all 0.2s;
}

.message-item.user .message-content {
    background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 100%);
    border: 1px solid rgba(255, 107, 107, 0.2);
}

.message-item.assistant .message-content {
    background: linear-gradient(135deg, #f0f7ff 0%, #e8f4ff 100%);
    border: 1px solid rgba(59, 130, 246, 0.15);
}

.message-text {
    font-size: 14px;
    line-height: 1.7;
    color: #333;
    word-wrap: break-word;
}

/* Streaming Indicator */
.streaming-indicator {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 20px;
    color: #667eea;
}

.typing-dots {
    display: flex;
    gap: 4px;
}

.typing-dots span {
    width: 8px;
    height: 8px;
    background: #667eea;
    border-radius: 50%;
    animation: typing 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(2) {
    animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
    animation-delay: 0.4s;
}

@keyframes typing {
    0%, 60%, 100% {
        transform: translateY(0);
        opacity: 0.4;
    }
    30% {
        transform: translateY(-6px);
        opacity: 1;
    }
}

.typing-text {
    font-size: 14px;
    font-weight: 500;
}

/* Chat Input */
.chat-input {
    border-top: 1px solid #f0f0f0;
    padding: 16px;
    background: #fff;
}

.chat-input :deep(.el-textarea__inner) {
    border: 2px solid #f0f0f0;
    border-radius: 14px;
    padding: 14px 18px;
    resize: none;
    transition: all 0.3s;
    font-size: 14px;
}

.chat-input :deep(.el-textarea__inner):focus {
    border-color: #667eea;
    box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.input-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 12px;
}

/* Preview Panel */
.preview-panel {
    flex: 1;
    min-width: 400px;
    max-width: 700px;
    background: #fff;
    border-radius: 20px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.preview-header {
    height: 52px;
    padding: 0 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid #f0f0f0;
    flex-shrink: 0;
}

.preview-title {
    font-size: 15px;
    font-weight: 600;
    color: #333;
}

.preview-actions {
    display: flex;
    gap: 4px;
}

.preview-container {
    flex: 1;
    padding: 16px;
    background: linear-gradient(145deg, #f8fafc 0%, #f1f5f9 100%);
}

.preview-iframe {
    width: 100%;
    height: 100%;
    border: none;
    border-radius: 12px;
    background: #fff;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

/* Preview Animation */
.preview-fade-enter-active {
    animation: previewSlideIn 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.preview-fade-leave-active {
    animation: previewSlideIn 0.4s cubic-bezier(0.4, 0, 0.2, 1) reverse;
}

@keyframes previewSlideIn {
    0% {
        opacity: 0;
        transform: translateX(40px) scale(0.95);
    }
    100% {
        opacity: 1;
        transform: translateX(0) scale(1);
    }
}

/* Deploy Dialog */
.deploy-success p {
    margin-bottom: 16px;
    color: #666;
}

.deploy-url {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px;
    background: #f7f8fa;
    border-radius: 10px;
}

.deploy-url code {
    flex: 1;
    font-size: 13px;
    word-break: break-all;
    color: #667eea;
}

/* Responsive */
@media (max-width: 1200px) {
    .panels-wrapper.with-preview {
        max-width: 100%;
    }
    
    .chat-panel {
        min-width: 320px;
    }
    
    .preview-panel {
        min-width: 320px;
    }
}

@media (max-width: 768px) {
    .chat-body {
        padding: 12px;
    }
    
    .panels-wrapper {
        flex-direction: column;
        max-width: 100%;
    }
    
    .chat-panel,
    .preview-panel {
        max-width: 100%;
        min-width: auto;
        border-radius: 16px;
    }
    
    .preview-panel {
        height: 50vh;
        flex: none;
    }
}
</style>
