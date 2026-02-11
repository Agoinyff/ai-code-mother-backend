<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { adminListChatHistory } from '@/api/chatHistory'
import type { ChatHistoryAdminQueryRequest } from '@/api/chatHistory'
import type { ChatHistoryVo } from '@/types/common'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// 表格数据
const tableData = ref<ChatHistoryVo[]>([])
const total = ref(0)
const loading = ref(false)

// 查询条件
const queryForm = reactive<ChatHistoryAdminQueryRequest>({
    pageNum: 1,
    pageSize: 20,
    appId: undefined,
    userId: undefined,
    messageType: '',
    message: '',
})

// 加载对话历史
async function loadHistory() {
    loading.value = true
    try {
        const res = await adminListChatHistory(queryForm)
        if (res.code === 0 && res.data) {
            tableData.value = res.data.records || []
            total.value = Number(res.data.total) || 0
        }
    } catch (error: any) {
        // 错误已由 request 拦截器处理
    } finally {
        loading.value = false
    }
}

// 搜索
function handleSearch() {
    queryForm.pageNum = 1
    loadHistory()
}

// 重置搜索
function handleReset() {
    queryForm.appId = undefined
    queryForm.userId = undefined
    queryForm.messageType = ''
    queryForm.message = ''
    queryForm.pageNum = 1
    loadHistory()
}

// 分页变化
function handlePageChange(page: number) {
    queryForm.pageNum = page
    loadHistory()
}

function handleSizeChange(size: number) {
    queryForm.pageSize = size
    queryForm.pageNum = 1
    loadHistory()
}

// 返回首页
function goHome() {
    router.push('/')
}

// 获取消息类型标签颜色
function getMessageTypeTag(type: string) {
    switch (type) {
        case 'user': return 'primary'
        case 'ai': return 'success'
        case 'error': return 'danger'
        default: return 'info'
    }
}

// 获取消息类型名称
function getMessageTypeName(type: string) {
    switch (type) {
        case 'user': return '用户'
        case 'ai': return 'AI'
        case 'error': return '错误'
        default: return type
    }
}

// 检查权限
onMounted(() => {
    if (!userStore.isLoggedIn || userStore.userInfo?.userRole !== 'admin') {
        ElMessage.error('无权访问，需要管理员权限')
        router.push('/')
        return
    }
    loadHistory()
})
</script>

<template>
    <div class="admin-chat-page">
        <!-- 顶部栏 -->
        <header class="page-header">
            <div class="header-left">
                <el-button text @click="goHome">
                    <el-icon><ArrowLeft /></el-icon>
                    返回首页
                </el-button>
                <span class="page-title">对话历史管理</span>
            </div>
        </header>

        <!-- 主内容 -->
        <div class="page-content">
            <!-- 搜索栏 -->
            <div class="search-bar">
                <el-input
                    v-model="queryForm.appId"
                    placeholder="应用ID"
                    clearable
                    style="width: 150px"
                    @keyup.enter="handleSearch"
                />
                <el-input
                    v-model="queryForm.userId"
                    placeholder="用户ID"
                    clearable
                    style="width: 150px"
                    @keyup.enter="handleSearch"
                />
                <el-select
                    v-model="queryForm.messageType"
                    placeholder="消息类型"
                    clearable
                    style="width: 120px"
                >
                    <el-option label="用户" value="user" />
                    <el-option label="AI" value="ai" />
                    <el-option label="错误" value="error" />
                </el-select>
                <el-input
                    v-model="queryForm.message"
                    placeholder="消息内容"
                    clearable
                    style="width: 200px"
                    @keyup.enter="handleSearch"
                />
                <el-button type="primary" @click="handleSearch">
                    <el-icon><Search /></el-icon>
                    搜索
                </el-button>
                <el-button @click="handleReset">重置</el-button>
            </div>

            <!-- 表格 -->
            <el-table
                :data="tableData"
                v-loading="loading"
                border
                stripe
                style="width: 100%"
            >
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="appId" label="应用ID" width="100" />
                <el-table-column prop="userId" label="用户ID" width="100" />
                <el-table-column label="消息类型" width="100">
                    <template #default="{ row }">
                        <el-tag :type="getMessageTypeTag(row.messageType)" size="small">
                            {{ getMessageTypeName(row.messageType) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="message" label="消息内容" min-width="300" show-overflow-tooltip />
                <el-table-column prop="createTime" label="创建时间" width="180">
                    <template #default="{ row }">
                        {{ new Date(row.createTime).toLocaleString('zh-CN') }}
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-wrapper">
                <el-pagination
                    v-model:current-page="queryForm.pageNum"
                    v-model:page-size="queryForm.pageSize"
                    :page-sizes="[10, 20, 50, 100]"
                    :total="total"
                    layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handleSizeChange"
                    @current-change="handlePageChange"
                />
            </div>
        </div>
    </div>
</template>

<style scoped>
.admin-chat-page {
    min-height: 100vh;
    background: linear-gradient(145deg, #f8fafc 0%, #e2e8f0 100%);
}

.page-header {
    height: 60px;
    padding: 0 24px;
    display: flex;
    align-items: center;
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
    position: sticky;
    top: 0;
    z-index: 100;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 16px;
}

.page-title {
    font-size: 18px;
    font-weight: 600;
    color: #1a1a1a;
}

.page-content {
    max-width: 1400px;
    margin: 0 auto;
    padding: 24px;
}

.search-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    padding: 20px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    flex-wrap: wrap;
}

.pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 20px;
    padding: 20px;
    background: #fff;
    border-radius: 12px;
}

:deep(.el-table) {
    border-radius: 12px;
    overflow: hidden;
}
</style>
