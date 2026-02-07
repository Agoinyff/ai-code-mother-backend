<script setup lang="ts">
import { computed } from 'vue'
import type { AppVo } from '@/types/app'
import { getPreviewUrl } from '@/api/sse'
import { ElDropdown, ElDropdownMenu, ElDropdownItem, ElTag, ElAvatar } from 'element-plus'

interface Props {
    app: AppVo
    showActions?: boolean
}

const props = withDefaults(defineProps<Props>(), {
    showActions: false,
})

const emit = defineEmits<{
    click: [app: AppVo]
    edit: [app: AppVo]
    delete: [app: AppVo]
}>()

// 计算预览图URL
const previewImage = computed(() => {
    if (props.app.cover) return props.app.cover
    // 如果有 deployKey，返回预览 URL
    if (props.app.deployKey) {
        return getPreviewUrl(props.app.deployKey)
    }
    // 默认占位图
    return null
})

// 格式化时间为相对时间
function formatRelativeTime(dateStr: string): string {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    const now = new Date()
    const diff = now.getTime() - date.getTime()

    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(diff / 3600000)
    const days = Math.floor(diff / 86400000)
    const weeks = Math.floor(days / 7)

    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days < 7) return `${days}天前`
    if (weeks < 4) return `${weeks}周前`
    return date.toLocaleDateString()
}

// 代码类型标签
const typeLabel = computed(() => {
    const typeMap: Record<string, { label: string; type: 'primary' | 'success' | 'warning' | 'danger' | 'info' }> = {
        html: { label: '网站', type: 'primary' },
        react: { label: '应用', type: 'success' },
        vue: { label: '应用', type: 'success' },
    }
    return typeMap[props.app.codeGenType] || { label: '工具', type: 'info' }
})
</script>

<template>
    <div class="app-card" @click="emit('click', app)">
        <!-- 预览图区域 -->
        <div class="card-preview">
            <iframe
                v-if="app.deployKey && !previewImage"
                :src="getPreviewUrl(app.deployKey)"
                class="preview-iframe"
                frameborder="0"
                sandbox="allow-scripts allow-same-origin"
            ></iframe>
            <img v-else-if="previewImage" :src="previewImage" alt="" class="preview-image" />
            <div v-else class="preview-placeholder">
                <el-icon size="40" color="var(--text-muted)"><Document /></el-icon>
            </div>

            <!-- Actions Menu -->
            <ElDropdown v-if="showActions" trigger="click" class="card-actions" @click.stop>
                <el-button type="default" circle size="small">
                    <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                    <ElDropdownMenu>
                        <ElDropdownItem @click.stop="emit('edit', app)">
                            <el-icon><Edit /></el-icon>
                            编辑
                        </ElDropdownItem>
                        <ElDropdownItem @click.stop="emit('delete', app)">
                            <el-icon><Delete /></el-icon>
                            删除
                        </ElDropdownItem>
                    </ElDropdownMenu>
                </template>
            </ElDropdown>
        </div>

        <!-- 卡片信息 -->
        <div class="card-info">
            <div class="card-meta">
                <ElAvatar
                    :size="28"
                    :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${app.userId}`"
                    class="user-avatar"
                />
                <div class="meta-content">
                    <div class="app-name">{{ app.appName || '未命名应用' }}</div>
                    <div class="app-time">创建于 {{ formatRelativeTime(app.createTime) }}</div>
                </div>
            </div>
            <ElTag v-if="app.codeGenType" :type="typeLabel.type" size="small" effect="light">
                {{ typeLabel.label }}
            </ElTag>
        </div>
    </div>
</template>

<style scoped>
.app-card {
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    cursor: pointer;
    transition: all 0.3s ease;
    border: 1px solid var(--border-light);
}

.app-card:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    transform: translateY(-4px);
}

.card-preview {
    position: relative;
    height: 180px;
    background: var(--bg-secondary);
    overflow: hidden;
}

.preview-iframe {
    width: 200%;
    height: 200%;
    transform: scale(0.5);
    transform-origin: top left;
    pointer-events: none;
}

.preview-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.preview-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #f0f5f5 0%, #e8f0f0 100%);
}

.card-actions {
    position: absolute;
    top: 12px;
    right: 12px;
    opacity: 0;
    transition: opacity 0.2s;
}

.app-card:hover .card-actions {
    opacity: 1;
}

.card-info {
    padding: 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.card-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;
    min-width: 0;
}

.meta-content {
    flex: 1;
    min-width: 0;
}

.app-name {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-primary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.app-time {
    font-size: 12px;
    color: var(--text-muted);
    margin-top: 2px;
}
</style>
