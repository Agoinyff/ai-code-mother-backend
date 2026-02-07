<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listMyApps, updateMyApp, deleteMyApp } from '@/api/app'
import type { AppVo } from '@/types/app'
import AppCard from '@/components/app/AppCard.vue'
import { ElMessage, ElDialog, ElInput, ElButton, ElPagination, ElEmpty, ElMessageBox } from 'element-plus'

const router = useRouter()

const apps = ref<AppVo[]>([])
const isLoading = ref(true)
const searchText = ref('')
const currentPage = ref(1)
const totalCount = ref(0)
const pageSize = 12

// 编辑相关
const editingApp = ref<AppVo | null>(null)
const editName = ref('')
const showEditDialog = ref(false)

// 加载应用列表
async function loadApps() {
    isLoading.value = true
    try {
        const res = await listMyApps({
            pageNum: currentPage.value,
            pageSize,
            appName: searchText.value || undefined,
        })
        apps.value = res.data.records
        totalCount.value = res.data.total
    } catch (error) {
        console.error('加载应用列表失败', error)
    } finally {
        isLoading.value = false
    }
}

onMounted(loadApps)

// 搜索
function handleSearch() {
    currentPage.value = 1
    loadApps()
}

// 点击应用卡片
function handleAppClick(app: AppVo) {
    router.push(`/chat/${app.id}`)
}

// 编辑应用
function handleEdit(app: AppVo) {
    editingApp.value = app
    editName.value = app.appName
    showEditDialog.value = true
}

// 保存编辑
async function saveEdit() {
    if (!editingApp.value || !editName.value.trim()) return

    try {
        await updateMyApp({
            id: editingApp.value.id,
            appName: editName.value.trim(),
        })
        // 更新列表
        const app = apps.value.find(a => a.id === editingApp.value!.id)
        if (app) {
            app.appName = editName.value.trim()
        }
        showEditDialog.value = false
        editingApp.value = null
        ElMessage.success('更新成功')
    } catch (error) {
        console.error('更新失败', error)
        ElMessage.error('更新失败，请重试')
    }
}

// 删除应用
async function handleDelete(app: AppVo) {
    try {
        await ElMessageBox.confirm(
            `确定要删除应用"${app.appName || '未命名'}"吗？`,
            '删除确认',
            {
                confirmButtonText: '删除',
                cancelButtonText: '取消',
                type: 'warning',
            }
        )

        await deleteMyApp(app.id)
        apps.value = apps.value.filter(a => a.id !== app.id)
        ElMessage.success('删除成功')
    } catch (error: any) {
        if (error !== 'cancel') {
            console.error('删除失败', error)
            ElMessage.error('删除失败，请重试')
        }
    }
}

// 翻页
function handlePageChange(page: number) {
    currentPage.value = page
    loadApps()
}
</script>

<template>
    <div class="my-apps-page">
        <div class="page-container">
            <!-- Header -->
            <div class="page-header">
                <h1 class="page-title">我的作品</h1>
                <div class="search-bar">
                    <ElInput
                        v-model="searchText"
                        placeholder="搜索应用..."
                        prefix-icon="Search"
                        clearable
                        @keyup.enter="handleSearch"
                        @clear="handleSearch"
                    />
                    <ElButton type="primary" @click="handleSearch">搜索</ElButton>
                </div>
            </div>

            <!-- Loading -->
            <div v-if="isLoading" class="loading-state">
                <el-icon class="is-loading" size="32"><Loading /></el-icon>
                <p>加载中...</p>
            </div>

            <!-- Empty -->
            <ElEmpty v-else-if="apps.length === 0" description="暂无应用">
                <RouterLink to="/">
                    <ElButton type="primary">创建第一个应用</ElButton>
                </RouterLink>
            </ElEmpty>

            <!-- Apps Grid -->
            <template v-else>
                <div class="apps-grid">
                    <AppCard
                        v-for="app in apps"
                        :key="app.id"
                        :app="app"
                        show-actions
                        @click="handleAppClick"
                        @edit="handleEdit"
                        @delete="handleDelete"
                    />
                </div>

                <!-- Pagination -->
                <div v-if="totalCount > pageSize" class="pagination">
                    <ElPagination
                        v-model:current-page="currentPage"
                        :page-size="pageSize"
                        :total="totalCount"
                        layout="prev, pager, next"
                        @current-change="handlePageChange"
                    />
                </div>
            </template>
        </div>

        <!-- Edit Dialog -->
        <ElDialog v-model="showEditDialog" title="编辑应用" width="400px" :close-on-click-modal="false">
            <ElInput v-model="editName" placeholder="请输入应用名称" />
            <template #footer>
                <ElButton @click="showEditDialog = false">取消</ElButton>
                <ElButton type="primary" @click="saveEdit">保存</ElButton>
            </template>
        </ElDialog>
    </div>
</template>

<style scoped>
.my-apps-page {
    min-height: 100vh;
    padding-top: 60px;
    background: var(--bg-secondary);
}

.page-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 32px 24px;
}

.page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 32px;
    gap: 24px;
    flex-wrap: wrap;
}

.page-title {
    font-size: 24px;
    font-weight: 600;
}

.search-bar {
    display: flex;
    gap: 12px;
}

.search-bar .el-input {
    width: 240px;
}

.apps-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 24px;
}

.loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px;
    color: var(--text-muted);
    gap: 16px;
}

.pagination {
    display: flex;
    justify-content: center;
    margin-top: 40px;
}

@media (max-width: 768px) {
    .page-header {
        flex-direction: column;
        align-items: stretch;
    }

    .search-bar {
        width: 100%;
    }

    .search-bar .el-input {
        flex: 1;
        width: auto;
    }
}
</style>
