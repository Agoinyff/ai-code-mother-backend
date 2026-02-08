<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { adminListApps, adminUpdateApp, adminDeleteApp } from '@/api/app'
import type { AppVo, AppAdminQueryRequest, AppAdminUpdateRequest } from '@/types/app'
import { ElMessage, ElMessageBox, ElTable, ElPagination, ElButton, ElInput, ElDialog, ElForm, ElFormItem, ElInputNumber, ElTag } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// 表格数据
const tableData = ref<AppVo[]>([])
const total = ref(0)
const loading = ref(false)

// 查询条件
const queryForm = reactive<AppAdminQueryRequest>({
    pageNum: 1,
    pageSize: 10,
    appName: '',
    codeGenType: '',
})

// 对话框
const dialogVisible = ref(false)
const formData = ref<AppAdminUpdateRequest>({
    id: '',
    appName: '',
    cover: '',
    priority: 0
})

// 加载应用列表
async function loadApps() {
    loading.value = true
    try {
        const res = await adminListApps(queryForm)
        if (res.code === 0 && res.data) {
            tableData.value = res.data.records || []
            total.value = Number(res.data.total) || 0
        }
    } catch (error: any) {
        ElMessage.error(error.message || '加载应用列表失败')
    } finally {
        loading.value = false
    }
}

// 搜索
function handleSearch() {
    queryForm.pageNum = 1
    loadApps()
}

// 重置搜索
function handleReset() {
    queryForm.appName = ''
    queryForm.codeGenType = ''
    queryForm.pageNum = 1
    loadApps()
}

// 分页变化
function handlePageChange(page: number) {
    queryForm.pageNum = page
    loadApps()
}

function handleSizeChange(size: number) {
    queryForm.pageSize = size
    queryForm.pageNum = 1
    loadApps()
}

// 打开编辑对话框
function handleEdit(row: AppVo) {
    formData.value = {
        id: row.id,
        appName: row.appName,
        cover: row.cover,
        priority: row.priority
    }
    dialogVisible.value = true
}

// 保存应用
async function handleSave() {
    if (!formData.value.appName) {
        ElMessage.warning('请输入应用名称')
        return
    }

    try {
        const res = await adminUpdateApp(formData.value)
        if (res.code === 0) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            loadApps()
        } else {
            ElMessage.error(res.message || '更新失败')
        }
    } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
    }
}

// 删除应用
async function handleDelete(row: AppVo) {
    try {
        await ElMessageBox.confirm('确定要删除该应用吗？删除后不可恢复！', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        })

        const res = await adminDeleteApp(row.id)
        if (res.code === 0) {
            ElMessage.success('删除成功')
            loadApps()
        } else {
            ElMessage.error(res.message || '删除失败')
        }
    } catch (error: any) {
        if (error !== 'cancel') {
            ElMessage.error(error.message || '删除失败')
        }
    }
}

// 查看应用详情
function handleView(row: AppVo) {
    router.push(`/chat/${row.id}`)
}

// 返回首页
function goHome() {
    router.push('/')
}

// 检查权限
onMounted(() => {
    if (!userStore.isLoggedIn || userStore.userInfo?.userRole !== 'admin') {
        ElMessage.error('无权访问，需要管理员权限')
        router.push('/')
        return
    }
    loadApps()
})
</script>

<template>
    <div class="admin-app-page">
        <!-- 顶部栏 -->
        <header class="page-header">
            <div class="header-left">
                <el-button text @click="goHome">
                    <el-icon><ArrowLeft /></el-icon>
                    返回首页
                </el-button>
                <span class="page-title">应用管理</span>
            </div>
        </header>

        <!-- 主内容 -->
        <div class="page-content">
            <!-- 搜索栏 -->
            <div class="search-bar">
                <el-input
                    v-model="queryForm.appName"
                    placeholder="应用名称"
                    clearable
                    style="width: 200px"
                    @keyup.enter="handleSearch"
                />
                <el-input
                    v-model="queryForm.codeGenType"
                    placeholder="生成类型"
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
                <el-table-column label="封面" width="100">
                    <template #default="{ row }">
                        <img v-if="row.cover" :src="row.cover" class="app-cover" />
                        <div v-else class="no-cover">无</div>
                    </template>
                </el-table-column>
                <el-table-column prop="appName" label="应用名称" min-width="150" />
                <el-table-column prop="codeGenType" label="生成类型" width="120">
                    <template #default="{ row }">
                        <el-tag>{{ row.codeGenType }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="priority" label="优先级" width="100" sortable>
                    <template #default="{ row }">
                        <el-tag :type="row.priority > 0 ? 'warning' : ''">
                            {{ row.priority }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="部署状态" width="120">
                    <template #default="{ row }">
                        <el-tag v-if="row.deployKey" type="success">已部署</el-tag>
                        <el-tag v-else type="info">未部署</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="userId" label="创建者ID" width="100" />
                <el-table-column prop="createTime" label="创建时间" width="180">
                    <template #default="{ row }">
                        {{ new Date(row.createTime).toLocaleString('zh-CN') }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="primary" size="small" @click="handleView(row)">
                            查看
                        </el-button>
                        <el-button link type="primary" size="small" @click="handleEdit(row)">
                            编辑
                        </el-button>
                        <el-button link type="danger" size="small" @click="handleDelete(row)">
                            删除
                        </el-button>
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

        <!-- 编辑对话框 -->
        <el-dialog v-model="dialogVisible" title="编辑应用" width="500px">
            <el-form :model="formData" label-width="100px">
                <el-form-item label="应用名称" required>
                    <el-input v-model="formData.appName" />
                </el-form-item>
                <el-form-item label="封面URL">
                    <el-input v-model="formData.cover" />
                    <div v-if="formData.cover" class="cover-preview">
                        <img :src="formData.cover" alt="封面预览" />
                    </div>
                </el-form-item>
                <el-form-item label="优先级">
                    <el-input-number 
                        v-model="formData.priority" 
                        :min="0" 
                        :max="999"
                        style="width: 100%"
                    />
                    <div class="form-tip">优先级越高，在精选列表中排序越靠前</div>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSave">保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
.admin-app-page {
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
    max-width: 1600px;
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
}

.app-cover {
    width: 60px;
    height: 60px;
    object-fit: cover;
    border-radius: 8px;
}

.no-cover {
    width: 60px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f5f5;
    border-radius: 8px;
    color: #999;
    font-size: 12px;
}

.pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 20px;
    padding: 20px;
    background: #fff;
    border-radius: 12px;
}

.cover-preview {
    margin-top: 12px;
}

.cover-preview img {
    max-width: 200px;
    max-height: 200px;
    border-radius: 8px;
    border: 1px solid #ddd;
}

.form-tip {
    margin-top: 4px;
    font-size: 12px;
    color: #999;
}

:deep(.el-table) {
    border-radius: 12px;
    overflow: hidden;
}
</style>
