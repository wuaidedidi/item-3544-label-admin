<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="login-bg-circle circle-1"></div>
      <div class="login-bg-circle circle-2"></div>
      <div class="login-bg-circle circle-3"></div>
    </div>
    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">
          <el-icon :size="36" color="#409EFF"><Monitor /></el-icon>
        </div>
        <h2 class="login-title">Label Admin</h2>
        <p class="login-subtitle">权限管理系统</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" :prefix-icon="Lock" show-password clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Monitor } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { useUserStore } from '../store/user'
import { sm2Encrypt, initSM2Key } from '../utils/sm2'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = ref({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

onMounted(() => {
  initSM2Key()
})

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const { encrypted, data: encPassword } = sm2Encrypt(form.value.password)
    const res = await login({
      username: form.value.username,
      password: encPassword,
      encrypted
    })
    if (res.code === 200) {
      userStore.setToken(res.data.token)
      userStore.setUserInfo(res.data.userInfo)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.login-bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  background: #fff;
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  left: -100px;
  animation: float 8s ease-in-out infinite;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  right: -50px;
  animation: float 6s ease-in-out infinite reverse;
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  right: 20%;
  animation: float 10s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-30px); }
}

.login-card {
  width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #e8f0fe, #d4e4fd);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.login-title {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 6px;
}

.login-subtitle {
  font-size: 14px;
  color: #909399;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  padding: 4px 12px;
  transition: all 0.3s;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409EFF inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409EFF inset;
}

.login-form :deep(.el-input__wrapper:-webkit-autofill),
.login-form :deep(input:-webkit-autofill) {
  -webkit-box-shadow: 0 0 0 1000px white inset !important;
  transition: background-color 5000s ease-in-out 0s;
}

.login-btn {
  width: 100%;
  height: 46px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.login-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
}

.register-link {
  color: #409EFF;
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
}

.register-link:hover {
  text-decoration: underline;
}

@media (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 32px 24px;
  }
}
</style>
