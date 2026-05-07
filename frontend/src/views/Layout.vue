<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo-area" @click="$router.push('/dashboard')">
        <el-icon :size="24" color="#409EFF"><Monitor /></el-icon>
        <span v-show="!isCollapse" class="logo-text">Label Admin</span>
      </div>
      <el-menu
        :default-active="$route.path"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="aside-menu"
        background-color="#1a1a2e"
        text-color="#a0a4b8"
        active-text-color="#409EFF"
      >
        <template v-for="item in filteredMenus" :key="item.path">
          <el-menu-item :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse" :size="20">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" class="user-avatar">
                {{ userStore.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.nickname || '用户' }}</span>
              <el-icon :size="14"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Monitor, Fold, Expand, ArrowDown, User, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const allMenuItems = [
  { path: '/dashboard', title: '仪表盘', icon: 'Odometer' },
  { path: '/users', title: '用户管理', icon: 'User', roles: ['ADMIN'] },
  { path: '/roles', title: '角色管理', icon: 'UserFilled', roles: ['ADMIN'] },
  { path: '/files', title: '文件管理', icon: 'FolderOpened' },
  { path: '/profile', title: '个人中心', icon: 'Setting' }
]

const filteredMenus = computed(() => {
  const roleCode = userStore.userInfo?.roleCode
  return allMenuItems.filter(item => {
    if (!item.roles || item.roles.length === 0) return true
    return item.roles.includes(roleCode)
  })
})

onMounted(() => {
  userStore.fetchUserInfo()
})

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
}

.layout-aside {
  background-color: #1a1a2e;
  transition: width 0.3s;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.logo-area {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
}

.aside-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.aside-menu::-webkit-scrollbar {
  width: 0;
}

.aside-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 8px;
  border-radius: 8px;
  transition: all 0.3s;
}

.aside-menu :deep(.el-menu-item:hover) {
  background-color: rgba(64, 158, 255, 0.1) !important;
}

.aside-menu :deep(.el-menu-item.is-active) {
  background-color: rgba(64, 158, 255, 0.15) !important;
  color: #409EFF !important;
}

.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.layout-header {
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  color: #606266;
  transition: color 0.3s;
}

.collapse-btn:hover {
  color: #409EFF;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.3s;
}

.user-info:hover {
  background: #f5f7fa;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.layout-main {
  background: #f0f2f5;
  overflow-y: auto;
  padding: 20px;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
