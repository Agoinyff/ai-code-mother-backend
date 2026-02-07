<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listFeaturedApps } from '@/api/app'
import type { AppVo } from '@/types/app'
import AppCard from '@/components/app/AppCard.vue'
import { ElPagination, ElEmpty } from 'element-plus'

const router = useRouter()

const apps = ref<AppVo[]>([])
const isLoading = ref(true)
const currentPage = ref(1)
const totalCount = ref(0)
const pageSize = 12

// 加载精选应用
async function loadApps() {
    isLoading.value = true
    try {
        const res = await listFeaturedApps({
            pageNum: currentPage.value,
            pageSize,
        })
        apps.value = res.data.records
        totalCount.value = res.data.total
    } catch (error) {
        console.error('加载精选应用失败', error)
    } finally {
        isLoading.value = false
    }
}

onMounted(loadApps)

// 点击应用
function handleAppClick(app: AppVo) {
    router.push(`/chat/${app.id}`)
}

// 翻页
function handlePageChange(page: number) {
    currentPage.value = page
    loadApps()
}
</script>

<template>
    <div class="featured-page">
        <div class="page-container">
            <!-- Header -->
            <div class="page-header">
                <h1 class="page-title">精选案例</h1>
                <p class="page-subtitle">探索社区优秀作品，获取灵感</p>
            </div>

            <!-- Loading -->
            <div v-if="isLoading" class="loading-state">
                <el-icon class="is-loading" size="32"><Loading /></el-icon>
                <p>加载中...</p>
            </div>

            <!-- Empty -->
            <ElEmpty v-else-if="apps.length === 0" description="暂无精选应用" />

            <!-- Apps Grid -->
            <template v-else>
                <div class="apps-grid">
                    <AppCard
                        v-for="app in apps"
                        :key="app.id"
                        :app="app"
                        @click="handleAppClick"
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
    </div>
</template>

<style scoped>
.featured-page {
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
    text-align: center;
    margin-bottom: 40px;
}

.page-title {
    font-size: 28px;
    font-weight: 600;
    margin-bottom: 8px;
}

.page-subtitle {
    font-size: 15px;
    color: var(--text-secondary);
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
</style>
