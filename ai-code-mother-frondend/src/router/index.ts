import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomePage.vue'),
    },
    {
      path: '/chat/:appId',
      name: 'chat',
      component: () => import('@/views/ChatPage.vue'),
      meta: { requiresAuth: true, hideNav: true },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginPage.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterPage.vue'),
    },
    {
      path: '/my-apps',
      name: 'my-apps',
      component: () => import('@/views/MyAppsPage.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/featured',
      name: 'featured',
      component: () => import('@/views/FeaturedAppsPage.vue'),
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/UserProfilePage.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: () => import('@/views/AdminUserManagePage.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/apps',
      name: 'admin-apps',
      component: () => import('@/views/AdminAppManagePage.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/chat-history',
      name: 'admin-chat-history',
      component: () => import('@/views/AdminChatHistoryPage.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
  ],
})

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()

  // 只有在有 token 但没有 userInfo 时才初始化用户信息
  // 登录刚成功时 userInfo 已经设置好了，不需要再调用 initUserInfo
  if (userStore.token && !userStore.userInfo) {
    try {
      await userStore.initUserInfo()
    } catch (error) {
      // 初始化失败，继续导航（initUserInfo 内部会清除无效 token）
      console.error('初始化用户信息失败', error)
    }
  }

  // 需要登录的页面
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({
      path: '/login',
      query: { redirect: to.fullPath },
    })
    return
  }

  // 需要管理员权限的页面
  if (to.meta.requiresAdmin && userStore.userInfo?.userRole !== 'admin') {
    next('/')
    return
  }

  // 已登录用户访问登录/注册页
  if ((to.name === 'login' || to.name === 'register') && userStore.isLoggedIn) {
    next('/')
    return
  }

  next()
})

export default router

