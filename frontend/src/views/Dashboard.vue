<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h2>仪表盘</h2>
      <p class="page-desc">欢迎回来，{{ userStore.nickname || '用户' }}</p>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-users">
          <div class="stat-icon">
            <el-icon :size="28"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-active">
          <div class="stat-icon">
            <el-icon :size="28"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.activeUsers || 0 }}</div>
            <div class="stat-label">活跃用户</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-roles">
          <div class="stat-icon">
            <el-icon :size="28"><UserFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalRoles || 0 }}</div>
            <div class="stat-label">角色数量</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-files">
          <div class="stat-icon">
            <el-icon :size="28"><FolderOpened /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalFiles || 0 }}</div>
            <div class="stat-label">文件总数</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :md="12">
        <el-card class="info-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">系统信息</span>
              <el-tag type="success" size="small">运行中</el-tag>
            </div>
          </template>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">系统名称</span>
              <span class="info-value">Label Admin 权限管理系统</span>
            </div>
            <div class="info-item">
              <span class="info-label">后端框架</span>
              <span class="info-value">Spring Boot 3.2 + MyBatis-Plus</span>
            </div>
            <div class="info-item">
              <span class="info-label">前端框架</span>
              <span class="info-value">Vue 3 + Element Plus</span>
            </div>
            <div class="info-item">
              <span class="info-label">数据库</span>
              <span class="info-value">MySQL 8.0</span>
            </div>
            <div class="info-item">
              <span class="info-label">加密传输</span>
              <span class="info-value">国密SM2非对称加密</span>
            </div>
            <div class="info-item">
              <span class="info-label">JDK版本</span>
              <span class="info-value">JDK 17</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="info-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">功能概览</span>
            </div>
          </template>
          <div class="feature-grid">
            <div class="feature-item">
              <el-icon :size="24" color="#409EFF"><Lock /></el-icon>
              <span>权限控制</span>
            </div>
            <div class="feature-item">
              <el-icon :size="24" color="#67C23A"><SetUp /></el-icon>
              <span>安全防护</span>
            </div>
            <div class="feature-item">
              <el-icon :size="24" color="#E6A23C"><User /></el-icon>
              <span>用户管理</span>
            </div>
            <div class="feature-item">
              <el-icon :size="24" color="#F56C6C"><UserFilled /></el-icon>
              <span>角色管理</span>
            </div>
            <div class="feature-item">
              <el-icon :size="24" color="#909399"><Document /></el-icon>
              <span>Excel导入导出</span>
            </div>
            <div class="feature-item">
              <el-icon :size="24" color="#409EFF"><Upload /></el-icon>
              <span>文件上传下载</span>
            </div>
            <div class="feature-item">
              <el-icon :size="24" color="#67C23A"><Key /></el-icon>
              <span>SM2加密传输</span>
            </div>
            <div class="feature-item">
              <el-icon :size="24" color="#E6A23C"><DataLine /></el-icon>
              <span>数据导出</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, CircleCheck, UserFilled, FolderOpened, Lock, SetUp, Document, Upload, Key, DataLine } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import { getStats } from '../api/dashboard'

const userStore = useUserStore()
const stats = ref({})

onMounted(async () => {
  try {
    const res = await getStats()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (e) {
    // handled by interceptor
  }
})
</script>

<style scoped>
.dashboard-container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.page-desc {
  font-size: 14px;
  color: #909399;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
  margin-bottom: 16px;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-users .stat-icon { background: linear-gradient(135deg, #e8f4fd, #d0ebff); color: #409EFF; }
.stat-active .stat-icon { background: linear-gradient(135deg, #e8f8e8, #d0f0d0); color: #67C23A; }
.stat-roles .stat-icon { background: linear-gradient(135deg, #fef3e2, #fde6c3); color: #E6A23C; }
.stat-files .stat-icon { background: linear-gradient(135deg, #fde8e8, #fcd0d0); color: #F56C6C; }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.info-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.info-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f2f5;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.info-list {
  padding: 4px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f7fa;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 14px;
  color: #909399;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  padding: 4px 0;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #f8f9fb;
  border-radius: 10px;
  transition: all 0.3s;
}

.feature-item:hover {
  background: #eef3fc;
  transform: translateX(4px);
}

.feature-item span {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
</style>
