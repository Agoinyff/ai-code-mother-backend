<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElForm, ElFormItem, ElInput, ElButton } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const form = reactive({
    userAccount: '',
    userPassword: '',
})
const isLoading = ref(false)

// 表单校验规则
const rules = reactive<FormRules>({
    userAccount: [
        { required: true, message: '请输入账号', trigger: 'blur' },
        { min: 4, message: '账号长度至少4位', trigger: 'blur' }
    ],
    userPassword: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少6位', trigger: 'blur' }
    ]
})

async function handleLogin() {
    if (!formRef.value) return
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    isLoading.value = true
    try {
        await userStore.login(form.userAccount, form.userPassword)
        ElMessage.success('登录成功')

        const redirect = route.query.redirect as string
        router.push(redirect || '/')
    } catch (error: any) {
        // 错误已由 request 拦截器处理
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
                <ElForm ref="formRef" :model="form" :rules="rules" class="login-form" @submit.prevent="handleLogin">
                    <ElFormItem prop="userAccount">
                        <ElInput
                            v-model="form.userAccount"
                            size="large"
                            placeholder="请输入账号"
                            prefix-icon="User"
                        />
                    </ElFormItem>
                    <ElFormItem prop="userPassword">
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
