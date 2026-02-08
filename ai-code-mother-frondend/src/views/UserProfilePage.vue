<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElDialog, ElButton, ElInput, ElAvatar, ElCard, ElDescriptions, ElDescriptionsItem } from 'element-plus'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const userInfo = computed(() => userStore.userInfo)

// 编辑对话框
const showEditDialog = ref(false)
const editForm = ref({
    userName: '',
    userAvatar: '',
    userProfile: ''
})

// 打开编辑对话框
function openEditDialog() {
    if (userInfo.value) {
        editForm.value = {
            userName: userInfo.value.userName || '',
            userAvatar: userInfo.value.userAvatar || '',
            userProfile: userInfo.value.userProfile || ''
        }
        showEditDialog.value = true
    }
}

// 保存用户信息
async function saveUserInfo() {
    // TODO: 实现用户信息更新API
    ElMessage.info('用户个人信息更新功能待后端支持')
    showEditDialog.value = false
}

// 复制邀请码
function copyShareCode() {
    if (userInfo.value?.shareCode) {
        navigator.clipboard.writeText(userInfo.value.shareCode)
        ElMessage.success('邀请码已复制到剪贴板')
    }
}

// 格式化VIP到期时间
function formatVipExpireTime(time: string | null): string {
    if (!time) return '未开通VIP'
    const date = new Date(time)
    const now = new Date()
    if (date < now) return '已过期'
    return date.toLocaleDateString('zh-CN')
}

// 返回首页
function goHome() {
    router.push('/')
}

onMounted(() => {
    if (!userInfo.value) {
        ElMessage.error('未登录，请先登录')
        router.push('/login')
    }
})
</script>

<template>
    <div class="profile-page">
        <!-- 顶部栏 -->
        <header class="profile-header">
            <div class="header-left">
                <el-button text @click="goHome">
                    <el-icon><ArrowLeft /></el-icon>
                    返回首页
                </el-button>
                <span class="page-title">个人中心</span>
            </div>
        </header>

        <!-- 主内容 -->
        <div class="profile-content" v-if="userInfo">
            <!-- 用户信息卡片 -->
            <el-card class="profile-card" shadow="hover">
                <div class="user-header">
                    <el-avatar :size="100" :src="userInfo.userAvatar || '/default-avatar.png'">
                        {{ userInfo.userName?.[0] || userInfo.userAccount[0] }}
                    </el-avatar>
                    <div class="user-info">
                        <h2>{{ userInfo.userName || userInfo.userAccount }}</h2>
                        <p class="user-account">@{{ userInfo.userAccount }}</p>
                        <div class="user-badges">
                            <el-tag v-if="userInfo.userRole === 'admin'" type="danger" effect="dark">
                                <el-icon><Star /></el-icon> 管理员
                            </el-tag>
                            <el-tag v-else type="info">普通用户</el-tag>
                            <el-tag 
                                v-if="userInfo.vipExpireTime && new Date(userInfo.vipExpireTime) > new Date()" 
                                type="warning" 
                                effect="dark"
                            >
                                <el-icon><Crown /></el-icon> VIP会员
                            </el-tag>
                        </div>
                    </div>
                    <el-button type="primary" @click="openEditDialog">
                        <el-icon><Edit /></el-icon>
                        编辑资料
                    </el-button>
                </div>

                <div class="user-profile" v-if="userInfo.userProfile">
                    <h3>个人简介</h3>
                    <p>{{ userInfo.userProfile }}</p>
                </div>
            </el-card>

            <!-- 详细信息 -->
            <el-card class="details-card" shadow="hover">
                <template #header>
                    <h3>账户详情</h3>
                </template>
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="用户账号">
                        {{ userInfo.userAccount }}
                    </el-descriptions-item>
                    <el-descriptions-item label="用户昵称">
                        {{ userInfo.userName || '未设置' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="用户角色">
                        <el-tag :type="userInfo.userRole === 'admin' ? 'danger' : 'info'">
                            {{ userInfo.userRole === 'admin' ? '管理员' : '普通用户' }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="注册时间">
                        {{ new Date(userInfo.createTime).toLocaleString('zh-CN') }}
                    </el-descriptions-item>
                </el-descriptions>
            </el-card>

            <!-- VIP信息 -->
            <el-card class="vip-card" shadow="hover">
                <template #header>
                    <h3><el-icon><Crown /></el-icon> VIP会员信息</h3>
                </template>
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="VIP状态">
                        <el-tag 
                            :type="userInfo.vipExpireTime && new Date(userInfo.vipExpireTime) > new Date() ? 'success' : 'info'"
                        >
                            {{ formatVipExpireTime(userInfo.vipExpireTime) }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="VIP编号">
                        {{ userInfo.vipCode || '未开通' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="剩余次数">
                        {{ userInfo.vipNumber ?? '未开通' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="过期时间">
                        {{ formatVipExpireTime(userInfo.vipExpireTime) }}
                    </el-descriptions-item>
                </el-descriptions>
            </el-card>

            <!-- 邀请信息 -->
            <el-card class="invite-card" shadow="hover">
                <template #header>
                    <h3><el-icon><Share /></el-icon> 邀请信息</h3>
                </template>
                <el-descriptions :column="1" border>
                    <el-descriptions-item label="我的邀请码">
                        <div class="share-code-wrapper">
                            <code class="share-code">{{ userInfo.shareCode }}</code>
                            <el-button type="primary" size="small" @click="copyShareCode">
                                <el-icon><CopyDocument /></el-icon>
                                复制
                            </el-button>
                        </div>
                    </el-descriptions-item>
                    <el-descriptions-item label="已邀请用户数">
                        <el-tag type="success">{{ userInfo.inviteUser || 0 }} 人</el-tag>
                    </el-descriptions-item>
                </el-descriptions>
            </el-card>
        </div>

        <!-- 编辑对话框 -->
        <el-dialog v-model="showEditDialog" title="编辑个人资料" width="500px">
            <el-input v-model="editForm.userName" placeholder="昵称" style="margin-bottom: 16px" />
            <el-input v-model="editForm.userAvatar" placeholder="头像URL" style="margin-bottom: 16px" />
            <el-input 
                v-model="editForm.userProfile" 
                type="textarea" 
                :rows="4" 
                placeholder="个人简介"
            />
            <template #footer>
                <el-button @click="showEditDialog = false">取消</el-button>
                <el-button type="primary" @click="saveUserInfo">保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
.profile-page {
    min-height: 100vh;
    background: linear-gradient(145deg, #f8fafc 0%, #e2e8f0 100%);
}

.profile-header {
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

.profile-content {
    max-width: 1000px;
    margin: 0 auto;
    padding: 32px 24px;
    display: flex;
    flex-direction: column;
    gap: 24px;
}

.profile-card {
    border-radius: 16px;
}

.user-header {
    display: flex;
    align-items: flex-start;
    gap: 24px;
    padding-bottom: 24px;
}

.user-info {
    flex: 1;
}

.user-info h2 {
    margin: 0 0 8px 0;
    font-size: 24px;
    color: #1a1a1a;
}

.user-account {
    margin: 0 0 12px 0;
    color: #666;
    font-size: 14px;
}

.user-badges {
    display: flex;
    gap: 8px;
}

.user-profile {
    padding-top: 24px;
    border-top: 1px solid #f0f0f0;
}

.user-profile h3 {
    margin: 0 0 12px 0;
    font-size: 16px;
    color: #333;
}

.user-profile p {
    margin: 0;
    color: #666;
    line-height: 1.6;
}

.details-card,
.vip-card,
.invite-card {
    border-radius: 16px;
}

.details-card h3,
.vip-card h3,
.invite-card h3 {
    margin: 0;
    font-size: 16px;
    color: #333;
    display: flex;
    align-items: center;
    gap: 8px;
}

.share-code-wrapper {
    display: flex;
    align-items: center;
    gap: 12px;
}

.share-code {
    flex: 1;
    padding: 8px 12px;
    background: #f7f8fa;
    border-radius: 6px;
    font-family: 'Courier New', monospace;
    font-size: 14px;
    color: #667eea;
}

@media (max-width: 768px) {
    .profile-content {
        padding: 16px;
    }

    .user-header {
        flex-direction: column;
        align-items: center;
        text-align: center;
    }

    :deep(.el-descriptions) {
        font-size: 13px;
    }
}
</style>
