<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { createApp, listFeaturedApps, listMyApps } from '@/api/app'
import { CODE_GEN_TYPES } from '@/config/api'
import type { AppVo } from '@/types/app'
import { ElButton, ElInput, ElMessage } from 'element-plus'
import AppCard from '@/components/app/AppCard.vue'

const router = useRouter()
const userStore = useUserStore()

const prompt = ref('')
const codeGenType = ref('html')
const isLoading = ref(false)
const featuredApps = ref<AppVo[]>([])
const myApps = ref<AppVo[]>([])
const loadingFeatured = ref(true)
const loadingMyApps = ref(true)

// 模板建议
const templates = [
    { label: '波普风电商页面', prompt: '创建一个波普风格的电商商品展示页面，使用鲜艳的色彩和大胆的图形' },
    { label: '企业网站', prompt: '创建一个专业的企业官网首页，包含导航、轮播图、服务介绍、关于我们等模块' },
    { label: '电商运营后台', prompt: '创建一个电商运营后台管理界面，包含数据统计、订单管理、商品管理等功能' },
    { label: '暗黑话题社区', prompt: '创建一个暗黑风格的话题讨论社区，支持帖子列表和详情查看' },
]

// 加载精选应用和我的应用
onMounted(async () => {
    try {
        const res = await listFeaturedApps({ pageNum: 1, pageSize: 3 })
        featuredApps.value = res.data.records
    } catch (error) {
        console.error('加载精选应用失败', error)
    } finally {
        loadingFeatured.value = false
    }

    if (userStore.isLoggedIn) {
        try {
            const res = await listMyApps({ pageNum: 1, pageSize: 3 })
            myApps.value = res.data.records
        } catch (error) {
            console.error('加载我的应用失败', error)
        } finally {
            loadingMyApps.value = false
        }
    } else {
        loadingMyApps.value = false
    }
})

// 选择模板
function selectTemplate(template: { label: string; prompt: string }) {
    prompt.value = template.prompt
}

// 创建应用
async function handleCreate() {
    if (!prompt.value.trim()) {
        ElMessage.warning('请输入描述内容')
        return
    }

    if (!userStore.isLoggedIn) {
        ElMessage.info('请先登录')
        router.push('/login')
        return
    }

    isLoading.value = true
    try {
        // 生成默认应用名称：取 prompt 前20个字符，或使用时间戳
        const trimmedPrompt = prompt.value.trim()
        const defaultAppName = trimmedPrompt.length > 20 
            ? trimmedPrompt.substring(0, 20) + '...' 
            : trimmedPrompt
        
        const res = await createApp({
            appName: defaultAppName || `应用_${Date.now()}`,
            initPrompt: trimmedPrompt,
            codeGenType: codeGenType.value,
        })
        console.log('创建应用响应:', res) // 调试日志
        const appId = res.data
        console.log('应用ID:', appId) // 调试日志
        if (appId) {
            router.push(`/chat/${appId}`)
        } else {
            ElMessage.error('创建应用失败：未获取到应用ID')
        }
    } catch (error: any) {
        // 错误已在 request.ts 中统一处理，这里不重复提示
    } finally {
        isLoading.value = false
    }
}

function handleAppClick(app: AppVo) {
    router.push(`/chat/${app.id}`)
}
</script>

<template>
    <div class="home-page">
        <!-- Hero Section -->
        <section class="hero-section">
            <div class="hero-content">
                <h1 class="hero-title">
                    <span>一句话</span>
                    <span class="logo-icon">🐱</span>
                    <span class="highlight">呈所想</span>
                </h1>
                <p class="hero-subtitle">与 AI 对话轻松创建应用和网站</p>

                <!-- Code Type Selector -->
                <div class="code-type-selector">
                    <div class="selector-label">
                        <el-icon><Document /></el-icon>
                        <span>选择生成类型</span>
                    </div>
                    <el-radio-group v-model="codeGenType" class="type-options">
                        <el-radio-button value="html">
                            <div class="radio-content">
                                <el-icon><DocumentCopy /></el-icon>
                                <div>
                                    <div class="radio-title">单页面 HTML</div>
                                    <div class="radio-desc">快速生成单个HTML页面</div>
                                </div>
                            </div>
                        </el-radio-button>
                        <el-radio-button value="react">
                            <div class="radio-content">
                                <el-icon><Files /></el-icon>
                                <div>
                                    <div class="radio-title">React 多页面</div>
                                    <div class="radio-desc">生成完整React项目</div>
                                </div>
                            </div>
                        </el-radio-button>
                        <el-radio-button value="vue">
                            <div class="radio-content">
                                <el-icon><Files /></el-icon>
                                <div>
                                    <div class="radio-title">Vue 多页面</div>
                                    <div class="radio-desc">生成完整Vue项目</div>
                                </div>
                            </div>
                        </el-radio-button>
                    </el-radio-group>
                </div>

                <!-- Prompt Input Card -->
                <div class="prompt-card">
                    <ElInput
                        v-model="prompt"
                        type="textarea"
                        :rows="3"
                        resize="none"
                        placeholder="使用 NoCode 创建一个高效的小工具，帮我计算......"
                        :disabled="isLoading"
                        class="prompt-textarea"
                    />
                    <div class="prompt-actions">
                        <div class="action-left">
                            <el-button text>
                                <el-icon><Upload /></el-icon>
                                上传
                            </el-button>
                            <el-button text>
                                <el-icon><MagicStick /></el-icon>
                                优化
                            </el-button>
                        </div>
                        <el-button
                            type="primary"
                            :loading="isLoading"
                            circle
                            size="large"
                            @click="handleCreate"
                        >
                            <el-icon v-if="!isLoading"><Top /></el-icon>
                        </el-button>
                    </div>
                </div>

                <!-- Template Suggestions -->
                <div class="template-suggestions">
                    <el-button
                        v-for="template in templates"
                        :key="template.label"
                        round
                        size="small"
                        @click="selectTemplate(template)"
                    >
                        {{ template.label }}
                    </el-button>
                </div>
            </div>
        </section>

        <!-- Content Section -->
        <section class="content-section">
            <!-- My Apps -->
            <div v-if="userStore.isLoggedIn && myApps.length > 0" class="section-block">
                <div class="section-header">
                    <h2 class="section-title">我的作品</h2>
                    <RouterLink to="/my-apps" class="view-all">查看全部 →</RouterLink>
                </div>
                <div class="apps-grid">
                    <AppCard
                        v-for="app in myApps"
                        :key="app.id"
                        :app="app"
                        @click="handleAppClick"
                    />
                </div>
            </div>

            <!-- Featured Apps -->
            <div class="section-block">
                <div class="section-header">
                    <h2 class="section-title">精选案例</h2>
                    <RouterLink to="/featured" class="view-all">查看全部 →</RouterLink>
                </div>
                <div v-if="loadingFeatured" class="loading-state">
                    <el-icon class="is-loading"><Loading /></el-icon>
                </div>
                <div v-else-if="featuredApps.length" class="apps-grid featured-grid">
                    <AppCard
                        v-for="app in featuredApps"
                        :key="app.id"
                        :app="app"
                        @click="handleAppClick"
                    />
                </div>
                <div v-else class="empty-state">
                    <p>暂无精选应用</p>
                </div>
            </div>
        </section>
    </div>
</template>

<style scoped>
.home-page {
    min-height: 100vh;
    padding-top: 60px;
}

.hero-section {
    background: var(--bg-gradient);
    padding: 80px 24px 60px;
    text-align: center;
}

.hero-content {
    max-width: 680px;
    margin: 0 auto;
}

.hero-title {
    font-size: 42px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
}

.hero-title .highlight {
    color: var(--primary-color);
}

.logo-icon {
    font-size: 48px;
}

.hero-subtitle {
    font-size: 16px;
    color: var(--text-secondary);
    margin-bottom: 32px;
}

/* Code Type Selector */
.code-type-selector {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 16px;
    padding: 20px;
    margin-bottom: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.selector-label {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;
    font-size: 14px;
    font-weight: 500;
    color: var(--text-primary);
}

.type-options {
    display: flex;
    gap: 12px;
    width: 100%;
}

.type-options :deep(.el-radio-button) {
    flex: 1;
}

.type-options :deep(.el-radio-button__inner) {
    width: 100%;
    padding: 16px 12px;
    border-radius: 12px;
    border: 2px solid var(--border-color);
    background: #fff;
    transition: all 0.3s;
}

.type-options :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-color: #667eea;
    color: #fff;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.radio-content {
    display: flex;
    align-items: center;
    gap: 10px;
    text-align: left;
}

.radio-content .el-icon {
    font-size: 24px;
    flex-shrink: 0;
}

.radio-title {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 2px;
}

.radio-desc {
    font-size: 11px;
    opacity: 0.8;
}


.prompt-card {
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
    padding: 20px;
    margin-bottom: 24px;
}

.prompt-textarea :deep(.el-textarea__inner) {
    border: none;
    box-shadow: none;
    padding: 0;
    font-size: 15px;
    line-height: 1.6;
}

.prompt-textarea :deep(.el-textarea__inner):focus {
    box-shadow: none;
}

.prompt-actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid var(--border-light);
}

.action-left {
    display: flex;
    gap: 8px;
}

.template-suggestions {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 12px;
}

.template-suggestions .el-button {
    background: transparent;
    border-color: var(--border-color);
    color: var(--text-secondary);
}

.template-suggestions .el-button:hover {
    border-color: var(--primary-color);
    color: var(--primary-color);
}

/* Content Section */
.content-section {
    max-width: 1200px;
    margin: 0 auto;
    padding: 40px 24px;
}

.section-block {
    margin-bottom: 48px;
}

.section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24px;
}

.section-title {
    font-size: 22px;
    font-weight: 600;
    color: var(--text-primary);
}

.view-all {
    font-size: 14px;
    color: var(--text-muted);
}

.view-all:hover {
    color: var(--primary-color);
}

.apps-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
}

.loading-state,
.empty-state {
    text-align: center;
    padding: 48px;
    color: var(--text-muted);
}

@media (max-width: 992px) {
    .apps-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media (max-width: 640px) {
    .hero-title {
        font-size: 28px;
    }

    .logo-icon {
        font-size: 32px;
    }

    .apps-grid {
        grid-template-columns: 1fr;
    }
}
</style>
