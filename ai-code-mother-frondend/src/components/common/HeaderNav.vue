<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElDropdown, ElDropdownMenu, ElDropdownItem, ElAvatar } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo)

async function handleLogout() {
    await userStore.logout()
    router.push('/')
}
</script>

<template>
    <header class="header-nav">
        <div class="header-content">
            <!-- Logo -->
            <RouterLink to="/" class="logo">
                <span class="logo-icon">🐱</span>
                <span class="logo-text">AI Code Mother</span>
            </RouterLink>

            <!-- Navigation Links -->
            <nav class="nav-links">
                <RouterLink to="/" class="nav-link">首页</RouterLink>
                <RouterLink to="/featured" class="nav-link">精选案例</RouterLink>
                <RouterLink v-if="isLoggedIn" to="/my-apps" class="nav-link">我的作品</RouterLink>
            </nav>

            <!-- User Actions -->
            <div class="user-actions">
                <template v-if="isLoggedIn">
                    <ElDropdown trigger="click">
                        <div class="user-info">
                            <ElAvatar
                                :size="32"
                                :src="userInfo?.userAvatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${userInfo?.userAccount}`"
                            />
                            <span class="username">{{ userInfo?.userName || userInfo?.userAccount }}</span>
                            <el-icon><ArrowDown /></el-icon>
                        </div>
                        <template #dropdown>
                            <ElDropdownMenu>
                                <ElDropdownItem @click="router.push('/my-apps')">
                                    我的作品
                                </ElDropdownItem>
                                <ElDropdownItem divided @click="handleLogout">
                                    退出登录
                                </ElDropdownItem>
                            </ElDropdownMenu>
                        </template>
                    </ElDropdown>
                </template>
                <template v-else>
                    <RouterLink to="/login" class="login-link">登录</RouterLink>
                    <RouterLink to="/register">
                        <el-button type="primary" round size="small">注册</el-button>
                    </RouterLink>
                </template>
            </div>
        </div>
    </header>
</template>

<style scoped>
.header-nav {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 1000;
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid var(--border-light);
}

.header-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.logo {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
    text-decoration: none;
}

.logo-icon {
    font-size: 24px;
}

.nav-links {
    display: flex;
    gap: 32px;
}

.nav-link {
    color: var(--text-secondary);
    font-size: 14px;
    transition: color 0.2s;
    padding: 8px 0;
}

.nav-link:hover,
.nav-link.router-link-active {
    color: var(--primary-color);
}

.user-actions {
    display: flex;
    align-items: center;
    gap: 16px;
}

.user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: 20px;
    transition: background 0.2s;
}

.user-info:hover {
    background: var(--bg-tertiary);
}

.username {
    font-size: 14px;
    color: var(--text-primary);
}

.login-link {
    font-size: 14px;
    color: var(--text-secondary);
}

.login-link:hover {
    color: var(--primary-color);
}

@media (max-width: 768px) {
    .nav-links {
        display: none;
    }

    .username {
        display: none;
    }
}
</style>
