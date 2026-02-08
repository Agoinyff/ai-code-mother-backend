<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { listUserVoByPage, addUser, updateUser, deleteUser } from '@/api/user'
import type { UserVo, UserQueryRequest, UserAddRequest, UserUpdateRequest } from '@/types/user'
import { ElMessage, ElMessageBox, ElTable, ElPagination, ElButton, ElInput, ElDialog, ElForm, ElFormItem, ElSelect, ElOption, ElTag } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// 表格数据
const tableData = ref<UserVo[]>([])
const total = ref(0)
const loading = ref(false)

// 查询条件
const queryForm = reactive<UserQueryRequest>({
    pageNum: 1,
    pageSize: 10,
    userAccount: '',
    userName: '',
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogType = ref<'add' | 'edit'>('add')

// 表单数据
const formData = ref<UserAddRequest & { id?: number }>({
    userAccount: '',
    userName: '',
    userAvatar: '',
    userProfile: '',
    userRole: 'user'
})

// 加载用户列表
async function loadUsers() {
    loading.value = true
    try {
        const res = await listUserVoByPage(queryForm)
        if (res.code === 0 && res.data) {
            tableData.value = res.data.records || []
            total.value = Number(res.data.total) || 0
        }
    } catch (error: any) {
        ElMessage.error(error.message || '加载用户列表失败')
    } finally {
        loading.value = false
    }
}

// 搜索
function handleSearch() {
    queryForm.pageNum = 1
    loadUsers()
}

// 重置搜索
function handleReset() {
    queryForm.userAccount = ''
    queryForm.userName = ''
    queryForm.pageNum = 1
    loadUsers()
}

// 分页变化
function handlePageChange(page: number) {
    queryForm.pageNum = page
    loadUsers()
}

function handleSizeChange(size: number) {
    queryForm.pageSize = size
    queryForm.pageNum = 1
    loadUsers()
}

// 打开新增对话框
function handleAdd() {
    dialogType.value = 'add'
    dialogTitle.value = '新增用户'
    formData.value = {
        userAccount: '',
        userName: '',
        userAvatar: '',
        userProfile: '',
        userRole: 'user'
    }
    dialogVisible.value = true
}

// 打开编辑对话框
function handleEdit(row: UserVo) {
    dialogType.value = 'edit'
    dialogTitle.value = '编辑用户'
    // 需要添加 id 字段，但 UserVo 没有 id，这里假设 userAccount 可以作为标识
    // 实际应该从后端获取包含 id 的完整用户信息
    formData.value = {
        userAccount: row.userAccount,
        userName: row.userName,
        userAvatar: row.userAvatar,
        userProfile: row.userProfile,
        userRole: row.userRole
    }
    dialogVisible.value = true
}

// 保存用户
async function handleSave() {
    if (!formData.value.userAccount) {
        ElMessage.warning('请输入用户账号')
        return
    }

    try {
        if (dialogType.value === 'add') {
            const res = await addUser(formData.value)
            if (res.code === 0) {
                ElMessage.success('新增用户成功')
                dialogVisible.value = false
                loadUsers()
            } else {
                ElMessage.error(res.message || '新增用户失败')
            }
        } else {
            // 编辑需要 ID，这里需要从其他地方获取
            ElMessage.info('编辑功能需要用户ID，请完善API')
        }
    } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
    }
}

// 删除用户
async function handleDelete(row: UserVo) {
    try {
        await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        })

        // 删除需要用户ID
        ElMessage.info('删除功能需要用户ID，请完善API')
    } catch {
        // 用户取消
    }
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
    loadUsers()
})
</script>

<template>
    <div class="admin-user-page">
        <!-- 顶部栏 -->
        <header class="page-header">
            <div class="header-left">
                <el-button text @click="goHome">
                    <el-icon><ArrowLeft /></el-icon>
                    返回首页
                </el-button>
                <span class="page-title">用户管理</span>
            </div>
        </header>

        <!-- 主内容 -->
        <div class="page-content">
            <!-- 搜索栏 -->
            <div class="search-bar">
                <el-input
                    v-model="queryForm.userAccount"
                    placeholder="用户账号"
                    clearable
                    style="width: 200px"
                    @keyup.enter="handleSearch"
                />
                <el-input
                    v-model="queryForm.userName"
                    placeholder="用户昵称"
                    clearable
                    style="width: 200px"
                    @keyup.enter="handleSearch"
                />
                <el-button type="primary" @click="handleSearch">
                    <el-icon><Search /></el-icon>
                    搜索
                </el-button>
                <el-button @click="handleReset">重置</el-button>
                <el-button type="success" @click="handleAdd">
                    <el-icon><Plus /></el-icon>
                    新增用户
                </el-button>
            </div>

            <!-- 表格 -->
            <el-table
                :data="tableData"
                v-loading="loading"
                border
                stripe
                style="width: 100%"
            >
                <el-table-column prop="userAccount" label="用户账号" width="150" />
                <el-table-column prop="userName" label="用户昵称" width="120" />
                <el-table-column label="头像" width="80">
                    <template #default="{ row }">
                        <el-avatar :size="40" :src="row.userAvatar">
                            {{ row.userName?.[0] || row.userAccount[0] }}
                        </el-avatar>
                    </template>
                </el-table-column>
                <el-table-column prop="userProfile" label="个人简介" min-width="150" show-overflow-tooltip />
                <el-table-column label="角色" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.userRole === 'admin' ? 'danger' : 'info'">
                            {{ row.userRole === 'admin' ? '管理员' : '普通用户' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="VIP状态" width="100">
                    <template #default="{ row }">
                        <el-tag 
                            v-if="row.vipExpireTime && new Date(row.vipExpireTime) > new Date()"
                            type="warning"
                        >
                            VIP
                        </el-tag>
                        <el-tag v-else type="info">普通</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="inviteUser" label="邀请人数" width="100" />
                <el-table-column prop="createTime" label="创建时间" width="180">
                    <template #default="{ row }">
                        {{ new Date(row.createTime).toLocaleString('zh-CN') }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right">
                    <template #default="{ row }">
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

        <!-- 新增/编辑对话框 -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
            <el-form :model="formData" label-width="100px">
                <el-form-item label="用户账号" required>
                    <el-input v-model="formData.userAccount" :disabled="dialogType === 'edit'" />
                </el-form-item>
                <el-form-item label="用户昵称">
                    <el-input v-model="formData.userName" />
                </el-form-item>
                <el-form-item label="头像URL">
                    <el-input v-model="formData.userAvatar" />
                </el-form-item>
                <el-form-item label="个人简介">
                    <el-input v-model="formData.userProfile" type="textarea" :rows="3" />
                </el-form-item>
                <el-form-item label="用户角色">
                    <el-select v-model="formData.userRole" style="width: 100%">
                        <el-option label="普通用户" value="user" />
                        <el-option label="管理员" value="admin" />
                    </el-select>
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
.admin-user-page {
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
