import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'DataAnalysis' }
      },
      {
        path: 'equipment',
        name: 'Equipment',
        component: () => import('@/views/equipment/index.vue'),
        meta: { title: '设备管理', icon: 'Monitor' }
      },
      {
        path: 'reservation',
        name: 'Reservation',
        component: () => import('@/views/reservation/index.vue'),
        meta: { title: '预约借用', icon: 'Calendar' }
      },
      {
        path: 'my-reservation',
        name: 'MyReservation',
        component: () => import('@/views/reservation/my.vue'),
        meta: { title: '我的预约', icon: 'Notebook' }
      },
      {
        path: 'repair',
        name: 'Repair',
        component: () => import('@/views/repair/index.vue'),
        meta: { title: '故障报修', icon: 'Tools' }
      },
      {
        path: 'maintenance',
        name: 'Maintenance',
        component: () => import('@/views/maintenance/index.vue'),
        meta: { title: '维护提醒', icon: 'Bell' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/index.vue'),
        meta: { title: '统计报表', icon: 'TrendCharts' }
      },
      {
        path: 'ai-chat',
        name: 'AIChat',
        component: () => import('@/views/ai/chat.vue'),
        meta: { title: 'AI助手', icon: 'ChatDotRound' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', icon: 'User' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'UserFilled', adminOnly: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token

  if (to.meta.requiresAuth !== false && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.name === 'Login' && token) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
