<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElForm, ElFormItem, ElInput, ElButton } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
    userAccount: '',
    userPassword: '',
    checkPassword: '',
})
const isLoading = ref(false)

async function handleRegister() {
    if (!form.value.userAccount || !form.value.userPassword || !form.value.checkPassword) {
        ElMessage.warning('请填写完整信息')
        return
    }

    if (form.value.userAccount.length < 4) {
        ElMessage.warning('账号长度至少4位')
        return
    }

    if (form.value.userPassword.length < 6) {
        ElMessage.warning('密码长度至少6位')
        return
    }

    if (form.value.userPassword !== form.value.checkPassword) {
        ElMessage.warning('两次密码不一致')
        return
    }

    isLoading.value = true
    try {
        await userStore.register(
            form.value.userAccount,
            form.value.userPassword,
            form.value.checkPassword
        )
        ElMessage.success('注册成功，请登录')
        router.push('/login')
    } catch (error: any) {
        ElMessage.error(error.message || '注册失败')
    } finally {
        isLoading.value = false
    }
}
</script>

<template>
    <div class="register-page">
        <div class="register-container">
            <div class="register-card">
                <!-- Logo -->
                <div class="register-header">
                    <RouterLink to="/" class="logo">
                        <span class="logo-icon">🐱</span>
                        <span class="logo-text">AI Code Mother</span>
                    </RouterLink>
                    <h2 class="register-title">创建账户</h2>
                    <p class="register-subtitle">加入我们，开始 AI 创作之旅</p>
                </div>

                <!-- Form -->
                <ElForm class="register-form" @submit.prevent="handleRegister">
                    <ElFormItem>
                        <ElInput
                            v-model="form.userAccount"
                            size="large"
                            placeholder="请输入账号（至少4位）"
                            prefix-icon="User"
                        />
                    </ElFormItem>
                    <ElFormItem>
                        <ElInput
                            v-model="form.userPassword"
                            size="large"
                            type="password"
                            placeholder="请输入密码（至少6位）"
                            prefix-icon="Lock"
                            show-password
                        />
                    </ElFormItem>
                    <ElFormItem>
                        <ElInput
                            v-model="form.checkPassword"
                            size="large"
                            type="password"
                            placeholder="请确认密码"
                            prefix-icon="Lock"
                            show-password
                        />
                    </ElFormItem>
                    <ElFormItem>
                        <ElButton
                            type="primary"
                            size="large"
                            :loading="isLoading"
                            class="submit-btn"
                            @click="handleRegister"
                        >
                            注册
                        </ElButton>
                    </ElFormItem>
                </ElForm>

                <!-- Footer -->
                <div class="register-footer">
                    <span>已有账号？</span>
                    <RouterLink to="/login" class="link">立即登录</RouterLink>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.register-page {
    min-height: 100vh;
    background: var(--bg-gradient);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24px;
}

.register-container {
    width: 100%;
    max-width: 400px;
}

.register-card {
    background: #fff;
    border-radius: 16px;
    padding: 40px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.register-header {
    text-align: center;
    margin-bottom: 32px;
}

.logo {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    font-size: 20px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 24px;
}

.logo-icon {
    font-size: 28px;
}

.register-title {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 8px;
}

.register-subtitle {
    font-size: 14px;
    color: var(--text-muted);
}

.register-form {
    margin-bottom: 24px;
}

.register-form :deep(.el-form-item) {
    margin-bottom: 20px;
}

.submit-btn {
    width: 100%;
}

.register-footer {
    text-align: center;
    font-size: 14px;
    color: var(--text-secondary);
}

.register-footer .link {
    color: var(--primary-color);
    font-weight: 500;
    margin-left: 4px;
}
</style>
