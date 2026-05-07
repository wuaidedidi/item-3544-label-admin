import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('../views/UserManagement.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['ADMIN'] }
      },
      {
        path: 'roles',
        name: 'RoleManagement',
        component: () => import('../views/RoleManagement.vue'),
        meta: { title: '角色管理', icon: 'UserFilled', roles: ['ADMIN'] }
      },
      {
        path: 'files',
        name: 'FileManagement',
        component: () => import('../views/FileManagement.vue'),
        meta: { title: '文件管理', icon: 'FolderOpened' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心', icon: 'Setting' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
    return
  }

  if ((to.path === '/login' || to.path === '/register') && token) {
    next('/dashboard')
    return
  }

  if (to.meta.roles && to.meta.roles.length > 0) {
    const userStore = useUserStore()
    const roleCode = userStore.userInfo?.roleCode
    if (!to.meta.roles.includes(roleCode)) {
      next('/dashboard')
      return
    }
  }

  next()
})

export default router
