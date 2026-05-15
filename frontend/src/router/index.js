import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/dispatcher',
    name: 'Dispatcher',
    component: () => import('../views/DispatcherDashboard.vue'),
    meta: { requiresAuth: true, role: 'DISPATCHER' }
  },
  {
    path: '/driver',
    name: 'Driver',
    component: () => import('../views/DriverDashboard.vue'),
    meta: { requiresAuth: true, role: 'DRIVER' }
  },
  {
    path: '/logs',
    name: 'MissionLogs',
    component: () => import('../views/MissionLogs.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/',
    redirect: to => {
      const { user } = useAuth()
      if (!user.value) return '/login'
      return user.value.role === 'DISPATCHER' ? '/dispatcher' : '/driver'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const { user } = useAuth()
  
  if (to.meta.requiresAuth && !user.value) {
    next('/login')
  } else if (to.meta.role && user.value.role !== to.meta.role) {
    next('/')
  } else {
    next()
  }
})

export default router
