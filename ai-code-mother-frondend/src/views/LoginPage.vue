<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElForm, ElFormItem, ElInput, ElButton } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = ref({
    userAccount: '',
    userPassword: '',
})
const isLoading = ref(false)

async function handleLogin() {
    if (!form.value.userAccount || !form.value.userPassword) {
        ElMessage.warning('请输入账号和密码')
        return
    }

    isLoading.value = true
    try {
        await userStore.login(form.value.userAccount, form.value.userPassword)
        ElMessage.success('登录成功')

        const redirect = route.query.redirect as string
        router.push(redirect || '/')
    } catch (error: any) {
        ElMessage.error(error.message || '登录失败')
    } finally {
        isLoading.value = false
    }
}
</script>

<template>
    <div class="login-page">
        <div class="login-container">
            <div class="login-card">
                <!-- Logo -->
                <div class="login-header">
                    <RouterLink to="/" class="logo">
                        <span class="logo-icon">🐱</span>
                        <span class="logo-text">AI Code Mother</span>
                    </RouterLink>
                    <h2 class="login-title">欢迎回来</h2>
                    <p class="login-subtitle">登录您的账户继续创作</p>
                </div>

                <!-- Form -->
                <ElForm class="login-form" @submit.prevent="handleLogin">
                    <ElFormItem>
                        <ElInput
                            v-model="form.userAccount"
                            size="large"
                            placeholder="请输入账号"
                            prefix-icon="User"
                        />
                    </ElFormItem>
                    <ElFormItem>
                        <ElInput
                            v-model="form.userPassword"
                            size="large"
                            type="password"
                            placeholder="请输入密码"
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
                            @click="handleLogin"
                        >
                            登录
                        </ElButton>
                    </ElFormItem>
                </ElForm>

                <!-- Footer -->
                <div class="login-footer">
                    <span>还没有账号？</span>
                    <RouterLink to="/register" class="link">立即注册</RouterLink>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.login-page {
    min-height: 100vh;
    background: var(--bg-gradient);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24px;
}

.login-container {
    width: 100%;
    max-width: 400px;
}

.login-card {
    background: #fff;
    border-radius: 16px;
    padding: 40px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.login-header {
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

.login-title {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 8px;
}

.login-subtitle {
    font-size: 14px;
    color: var(--text-muted);
}

.login-form {
    margin-bottom: 24px;
}

.login-form :deep(.el-form-item) {
    margin-bottom: 20px;
}

.submit-btn {
    width: 100%;
}

.login-footer {
    text-align: center;
    font-size: 14px;
    color: var(--text-secondary);
}

.login-footer .link {
    color: var(--primary-color);
    font-weight: 500;
    margin-left: 4px;
}
</style>
