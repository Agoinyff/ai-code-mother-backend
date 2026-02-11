<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElForm, ElFormItem, ElInput, ElButton } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const form = reactive({
    userAccount: '',
    userPassword: '',
    checkPassword: '',
})
const isLoading = ref(false)

// 自定义校验：确认密码
const validateCheckPassword = (_rule: any, value: string, callback: any) => {
    if (value !== form.userPassword) {
        callback(new Error('两次密码不一致'))
    } else {
        callback()
    }
}

// 表单校验规则
const rules = reactive<FormRules>({
    userAccount: [
        { required: true, message: '请输入账号', trigger: 'blur' },
        { min: 4, message: '账号长度至少4位', trigger: 'blur' }
    ],
    userPassword: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少6位', trigger: 'blur' }
    ],
    checkPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: validateCheckPassword, trigger: 'blur' }
    ]
})

async function handleRegister() {
    if (!formRef.value) return
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    isLoading.value = true
    try {
        await userStore.register(
            form.userAccount,
            form.userPassword,
            form.checkPassword
        )
        ElMessage.success('注册成功，请登录')
        router.push('/login')
    } catch (error: any) {
        // 错误已由 request 拦截器处理
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
                <ElForm ref="formRef" :model="form" :rules="rules" class="register-form" @submit.prevent="handleRegister">
                    <ElFormItem prop="userAccount">
                        <ElInput
                            v-model="form.userAccount"
                            size="large"
                            placeholder="请输入账号（至少4位）"
                            prefix-icon="User"
                        />
                    </ElFormItem>
                    <ElFormItem prop="userPassword">
                        <ElInput
                            v-model="form.userPassword"
                            size="large"
                            type="password"
                            placeholder="请输入密码（至少6位）"
                            prefix-icon="Lock"
                            show-password
                        />
                    </ElFormItem>
                    <ElFormItem prop="checkPassword">
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
