<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { deployApp } from '@/api/app'
import { getDeployVersions, rollbackDeploy, stopDeploy, type DeployHistory } from '@/api/deploy'
import { getPreviewUrl } from '@/api/sse'
import { ElMessage, ElDialog, ElButton, ElInput } from 'element-plus'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const appId = computed(() => String(route.params.appId))
const chatMessages = computed(() => appStore.chatMessages)
const currentApp = computed(() => appStore.currentApp)
const isGenerating = computed(() => appStore.isGenerating)
const hasMoreHistory = computed(() => appStore.hasMoreHistory)
const isLoadingHistory = computed(() => appStore.isLoadingHistory)

// 预览刷新 key（用于强制刷新 iframe）
const previewKey = ref(0)
const iframeRef = ref<HTMLIFrameElement>()

// 是否显示预览面板（计算属性，有预览URL就显示）
const showPreview = computed(() => !!previewUrl.value)

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

// 监听生成状态变化，生成完成后刷新预览
watch(isGenerating, (newVal, oldVal) => {
    if (oldVal === true && newVal === false) {
        // 生成刚完成，刷新预览
        setTimeout(() => {
            refreshPreview()
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
const deployVersions = ref<DeployHistory[]>([])
const isLoadingVersions = ref(false)
const isRollingBack = ref(false)

// 已部署状态管理
const runningDeploy = ref<DeployHistory | null>(null)  // 当前运行中的部署
const showManageDrawer = ref(false)                    // 部署管理抗屉
const isStopping = ref(false)                          // 停止中状态

// 是否已部署（有运行中的容器）
const isDeployed = computed(() => runningDeploy.value !== null)

async function handleDeploy() {
    if (!currentApp.value) return
    isDeploying.value = true
    try {
        const res = await deployApp({ appId: currentApp.value.id })
        deployUrl.value = res.data
        showDeployDialog.value = true
        // 刷新应用信息获取最新 deployKey
        await appStore.loadApp(appId.value)
        // 加载版本历史
        await loadVersions()
        // 更新运行中部署状态
        await loadRunningDeploy()
    } catch (error: any) {
        ElMessage.error(error.message || '部署失败')
    } finally {
        isDeploying.value = false
    }
}

async function loadRunningDeploy() {
    if (!currentApp.value) return
    try {
        const res = await getDeployVersions(currentApp.value.id)
        const versions = res.data || []
        runningDeploy.value = versions.find(v => v.status === 'RUNNING') || null
    } catch (e) {
        console.error('加载运行状态失败', e)
    }
}

async function handleStopDeploy() {
    if (!currentApp.value) return
    isStopping.value = true
    try {
        await stopDeploy(currentApp.value.id)
        runningDeploy.value = null
        showManageDrawer.value = false
        await appStore.loadApp(appId.value)
        ElMessage.success('应用已成功下线')
    } catch (error: any) {
        ElMessage.error(error.message || '下线失败')
    } finally {
        isStopping.value = false
    }
}

function openManageDrawer() {
    loadVersions()
    showManageDrawer.value = true
}

async function loadVersions() {
    if (!currentApp.value) return
    isLoadingVersions.value = true
    try {
        const res = await getDeployVersions(currentApp.value.id)
        deployVersions.value = res.data || []
    } catch (e) {
        console.error('加载版本历史失败', e)
    } finally {
        isLoadingVersions.value = false
    }
}

async function handleRollback(version: number) {
    if (!currentApp.value) return
    isRollingBack.value = true
    try {
        const res = await rollbackDeploy(currentApp.value.id, version)
        deployUrl.value = res.data
        ElMessage.success(`已回滚到 v${version}`)
        await loadVersions()
    } catch (error: any) {
        ElMessage.error(error.message || '回滚失败')
    } finally {
        isRollingBack.value = false
    }
}

function copyDeployUrl() {
    navigator.clipboard.writeText(deployUrl.value)
    ElMessage.success('链接已复制')
}

function openDeployUrl() {
    window.open(deployUrl.value, '_blank')
}

// 预览 URL（使用 StaticResourceController 接口，始终指向 code_output 目录）
const previewUrl = computed(() => {
    if (currentApp.value) {
        // 始终用 {codeGenType}_{appId} 格式，对应 code_output/{codeGenType}_{appId}/ 目录
        // deployKey 是部署容器的标识，不是预览目录名
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
        await appStore.loadApp(appId.value)
        // 历史消息已在 loadApp 中自动加载
        scrollToBottom()
        // 检查是否已有运行中的部署
        await loadRunningDeploy()

        // 如果从主页跳转过来携带了初始消息，自动发送
        const initMessage = route.query.initMessage as string
        if (initMessage && initMessage.trim()) {
            await appStore.sendMessage(initMessage.trim())
            scrollToBottom()
        }
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

// 加载更多历史消息
async function loadMoreHistory() {
    try {
        // 记录当前滚动高度
        const currentScrollHeight = chatListRef.value?.scrollHeight || 0
        
        await appStore.loadChatHistory(true)
        
        // 保持滚动位置在新加载内容的底部
        await nextTick()
        const newScrollHeight = chatListRef.value?.scrollHeight || 0
        if (chatListRef.value) {
            chatListRef.value.scrollTop = newScrollHeight - currentScrollHeight
        }
    } catch (error) {
        ElMessage.error('加载历史消息失败')
    }
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
                <!-- 已部署状态徽章 -->
                <div v-if="isDeployed" class="deployed-badge" @click="openManageDrawer">
                    <span class="deployed-dot"></span>
                    <span class="deployed-text">已上线</span>
                    <el-button text size="small" class="manage-btn">
                        管理
                        <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                    </el-button>
                </div>
                <el-button
                    v-if="showPreview && previewUrl"
                    type="primary"
                    :loading="isDeploying"
                    @click="handleDeploy"
                    class="deploy-btn"
                >
                    <el-icon><Upload /></el-icon>
                    {{ isDeployed ? '重新部署' : '部署上线' }}
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
                        <!-- 加载更多按钮 -->
                        <div v-if="hasMoreHistory" class="load-more-container">
                            <el-button 
                                text 
                                :loading="isLoadingHistory"
                                @click="loadMoreHistory"
                                class="load-more-btn"
                            >
                                <el-icon v-if="!isLoadingHistory"><ArrowUp /></el-icon>
                                {{ isLoadingHistory ? '加载中...' : '加载更多历史消息' }}
                            </el-button>
                        </div>
                        
                        <div
                            v-for="(msg, index) in chatMessages"
                            :key="msg.id"
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
        <ElDialog v-model="showDeployDialog" title="🎉 部署成功" width="600px" center>
            <div class="deploy-success">
                <p>您的应用已成功部署，访问链接：</p>
                <div class="deploy-url">
                    <code>{{ deployUrl }}</code>
                    <el-button type="primary" size="small" @click="copyDeployUrl">复制</el-button>
                </div>

                <!-- 版本历史 -->
                <div v-if="deployVersions.length > 0" class="version-history">
                    <h4 style="margin: 16px 0 8px; color: #666;">📋 部署版本历史</h4>
                    <el-table :data="deployVersions" size="small" :loading="isLoadingVersions" max-height="250">
                        <el-table-column prop="version" label="版本" width="70" align="center">
                            <template #default="{ row }">
                                <el-tag size="small">v{{ row.version }}</el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column prop="status" label="状态" width="90" align="center">
                            <template #default="{ row }">
                                <el-tag :type="row.status === 'RUNNING' ? 'success' : 'info'" size="small">
                                    {{ row.status }}
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column prop="createTime" label="部署时间" width="170" />
                        <el-table-column label="操作" width="100" align="center">
                            <template #default="{ row }">
                                <el-button
                                    v-if="row.status !== 'RUNNING'"
                                    type="warning"
                                    size="small"
                                    :loading="isRollingBack"
                                    @click="handleRollback(row.version)"
                                >
                                    回滚
                                </el-button>
                                <el-tag v-else type="success" size="small">当前版本</el-tag>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </div>
            <template #footer>
                <el-button @click="showDeployDialog = false">关闭</el-button>
                <el-button type="primary" @click="openDeployUrl">立即访问</el-button>
            </template>
        </ElDialog>

        <!-- 部署管理抽屉 -->
        <el-drawer
            v-model="showManageDrawer"
            title="🚀 部署管理"
            direction="rtl"
            size="420px"
            modal-class="deploy-drawer-modal"
        >
            <div class="manage-drawer-content">
                <!-- 当前运行状态 -->
                <div v-if="runningDeploy" class="running-info-card">
                    <div class="running-status">
                        <span class="status-dot running"></span>
                        <span class="status-label">运行中</span>
                        <el-tag type="success" size="small">v{{ runningDeploy.version }}</el-tag>
                    </div>
                    <div class="deploy-url-row">
                        <el-icon><Link /></el-icon>
                        <a :href="runningDeploy.deployUrl" target="_blank" class="deploy-link">
                            {{ runningDeploy.deployUrl }}
                        </a>
                    </div>
                    <div class="deploy-time">
                        <el-icon><Clock /></el-icon>
                        <span>部署于 {{ runningDeploy.createTime }}</span>
                    </div>
                    <div class="running-actions">
                        <el-button
                            type="primary"
                            @click="() => window.open(runningDeploy!.deployUrl, '_blank')"
                        >
                            <el-icon><Link /></el-icon>
                            访问网站
                        </el-button>
                        <el-button
                            @click="() => { navigator.clipboard.writeText(runningDeploy!.deployUrl); ElMessage.success('已复制') }"
                        >
                            <el-icon><CopyDocument /></el-icon>
                            复制地址
                        </el-button>
                        <el-button
                            type="danger"
                            :loading="isStopping"
                            @click="handleStopDeploy"
                        >
                            <el-icon><CircleClose /></el-icon>
                            下线应用
                        </el-button>
                    </div>
                </div>

                <el-divider />

                <!-- 版本历史 -->
                <div class="version-section">
                    <div class="version-section-title">
                        <span>📋 部署版本历史</span>
                        <el-button text size="small" @click="loadVersions" :loading="isLoadingVersions">
                            <el-icon><Refresh /></el-icon>
                        </el-button>
                    </div>
                    <div v-if="deployVersions.length === 0 && !isLoadingVersions" class="no-versions">
                        暂无部署记录
                    </div>
                    <div
                        v-for="item in deployVersions"
                        :key="item.id"
                        class="version-item"
                        :class="{ 'version-running': item.status === 'RUNNING' }"
                    >
                        <div class="version-item-left">
                            <el-tag
                                :type="item.status === 'RUNNING' ? 'success' : 'info'"
                                size="small"
                            >v{{ item.version }}</el-tag>
                            <span class="version-time">{{ item.createTime }}</span>
                        </div>
                        <div class="version-item-right">
                            <el-tag v-if="item.status === 'RUNNING'" type="success" size="small" effect="plain">当前</el-tag>
                            <el-button
                                v-else
                                size="small"
                                type="warning"
                                plain
                                :loading="isRollingBack"
                                @click="handleRollback(item.version)"
                            >回滚</el-button>
                        </div>
                    </div>
                </div>
            </div>
        </el-drawer>
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
    background: rgba(255, 255, 255, 0.95);
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

.header-right {
    display: flex;
    align-items: center;
    gap: 8px;
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

/* 加载更多容器 */
.load-more-container {
    text-align: center;
    padding: 12px 0;
    margin-bottom: 16px;
}

.load-more-btn {
    color: #667eea;
    font-size: 14px;
    transition: all 0.3s;
}

.load-more-btn:hover {
    color: #5a6fd6;
    transform: translateY(-2px);
}

/* ===== 已部署状态徽章 ===== */
.deployed-badge {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 4px 12px 4px 10px;
    background: rgba(52, 199, 89, 0.1);
    border: 1px solid rgba(52, 199, 89, 0.3);
    border-radius: 20px;
    cursor: pointer;
    transition: all 0.2s;
    margin-right: 8px;
}

.deployed-badge:hover {
    background: rgba(52, 199, 89, 0.18);
    border-color: rgba(52, 199, 89, 0.5);
}

.deployed-dot {
    width: 8px;
    height: 8px;
    background: #34c759;
    border-radius: 50%;
    animation: pulse-green 2s infinite;
    flex-shrink: 0;
}

@keyframes pulse-green {
    0%, 100% { box-shadow: 0 0 0 0 rgba(52, 199, 89, 0.4); }
    50% { box-shadow: 0 0 0 5px rgba(52, 199, 89, 0); }
}

.deployed-text {
    font-size: 13px;
    font-weight: 600;
    color: #34c759;
}

.manage-btn {
    color: #34c759 !important;
    padding: 0 !important;
    font-size: 13px !important;
}

/* ===== 部署管理抽屉内容 ===== */
.manage-drawer-content {
    padding: 0 4px;
}

.running-info-card {
    background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
    border: 1px solid #bbf7d0;
    border-radius: 12px;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.running-status {
    display: flex;
    align-items: center;
    gap: 8px;
}

.status-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    flex-shrink: 0;
}

.status-dot.running {
    background: #22c55e;
    animation: pulse-green 2s infinite;
}

.status-label {
    font-size: 14px;
    font-weight: 600;
    color: #15803d;
}

.deploy-url-row {
    display: flex;
    align-items: center;
    gap: 8px;
    background: rgba(255,255,255,0.7);
    border-radius: 8px;
    padding: 8px 12px;
}

.deploy-url-row .el-icon {
    color: #667eea;
    flex-shrink: 0;
}

.deploy-link {
    font-size: 13px;
    color: #667eea;
    text-decoration: none;
    word-break: break-all;
    font-weight: 500;
}

.deploy-link:hover {
    text-decoration: underline;
}

.deploy-time {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #6b7280;
}

.running-actions {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

.running-actions .el-button {
    flex: 1;
    min-width: 90px;
}

/* ===== 版本历史列表 ===== */
.version-section {
    margin-top: 4px;
}

.version-section-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 12px;
}

.no-versions {
    text-align: center;
    color: #9ca3af;
    font-size: 13px;
    padding: 24px 0;
}

.version-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 12px;
    border-radius: 8px;
    border: 1px solid #f3f4f6;
    margin-bottom: 8px;
    background: #fafafa;
    transition: all 0.2s;
}

.version-item:hover {
    border-color: #e5e7eb;
    background: #f9fafb;
}

.version-item.version-running {
    border-color: #bbf7d0;
    background: #f0fdf4;
}

.version-item-left {
    display: flex;
    align-items: center;
    gap: 8px;
}

.version-time {
    font-size: 12px;
    color: #9ca3af;
}

.version-item-right {
    flex-shrink: 0;
}

/* 消除抽屉顶部留空 */
:deep(.el-drawer__header) {
    margin-bottom: 0;
    padding: 16px 20px 12px;
    border-bottom: 1px solid #f0f0f0;
}

:deep(.el-drawer__body) {
    padding: 16px 20px;
}
</style>
