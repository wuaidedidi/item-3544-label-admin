<template>
  <div class="page-container">
    <div class="page-header">
      <h2>个人中心</h2>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :md="8">
        <el-card class="profile-card" shadow="never">
          <div class="avatar-section">
            <el-avatar :size="80" class="profile-avatar">
              {{ userStore.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <h3 class="profile-name">{{ profile.nickname || profile.username }}</h3>
            <el-tag :type="profile.roleCode === 'ADMIN' ? 'danger' : 'info'" size="small">
              {{ profile.roleName || '未分配角色' }}
            </el-tag>
          </div>
          <div class="profile-info-list">
            <div class="profile-info-item">
              <el-icon><User /></el-icon>
              <span class="label">用户名</span>
              <span class="value">{{ profile.username }}</span>
            </div>
            <div class="profile-info-item">
              <el-icon><Message /></el-icon>
              <span class="label">邮箱</span>
              <span class="value">{{ profile.email || '未设置' }}</span>
            </div>
            <div class="profile-info-item">
              <el-icon><Phone /></el-icon>
              <span class="label">手机</span>
              <span class="value">{{ profile.phone || '未设置' }}</span>
            </div>
            <div class="profile-info-item">
              <el-icon><Calendar /></el-icon>
              <span class="label">注册时间</span>
              <span class="value">{{ profile.createdAt || '-' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="16">
        <el-card class="edit-card" shadow="never">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="info">
              <el-form ref="infoFormRef" :model="infoForm" :rules="infoRules" label-width="80px" class="edit-form">
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="infoForm.nickname" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="infoForm.email" placeholder="请输入邮箱 (选填)" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="infoForm.phone" placeholder="请输入手机号 (选填)" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="infoLoading" @click="handleUpdateInfo">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="修改密码" name="password">
              <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" class="edit-form">
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码 (至少6位)" show-password />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="pwdLoading" @click="handleChangePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Message, Phone, Calendar } from '@element-plus/icons-vue'
import { getProfile, updateProfile, changePassword } from '../api/profile'
import { useUserStore } from '../store/user'
import { sm2Encrypt, initSM2Key } from '../utils/sm2'

const userStore = useUserStore()
const activeTab = ref('info')
const infoLoading = ref(false)
const pwdLoading = ref(false)
const infoFormRef = ref(null)
const pwdFormRef = ref(null)

const profile = ref({})
const infoForm = reactive({ nickname: '', email: '', phone: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateEmail = (rule, value, callback) => {
  if (value && value.trim()) {
    if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
      callback(new Error('请输入正确的邮箱格式'))
    } else { callback() }
  } else { callback() }
}

const validatePhone = (rule, value, callback) => {
  if (value && value.trim()) {
    if (!/^1[3-9]\d{9}$/.test(value)) {
      callback(new Error('请输入正确的手机号格式'))
    } else { callback() }
  } else { callback() }
}

const validateConfirmPwd = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else { callback() }
}

const infoRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ validator: validateEmail, trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }]
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码长度不少于6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }, { validator: validateConfirmPwd, trigger: 'blur' }]
}

onMounted(async () => {
  await initSM2Key()
  await loadProfile()
})

async function loadProfile() {
  try {
    const res = await getProfile()
    if (res.code === 200) {
      profile.value = res.data
      infoForm.nickname = res.data.nickname || ''
      infoForm.email = res.data.email || ''
      infoForm.phone = res.data.phone || ''
    }
  } catch (e) {}
}

async function handleUpdateInfo() {
  const valid = await infoFormRef.value.validate().catch(() => false)
  if (!valid) return

  infoLoading.value = true
  try {
    const res = await updateProfile({
      nickname: infoForm.nickname,
      email: infoForm.email || '',
      phone: infoForm.phone || ''
    })
    if (res.code === 200) {
      ElMessage.success('信息修改成功')
      profile.value = res.data
      userStore.updateUserInfo({
        nickname: res.data.nickname,
        email: res.data.email,
        phone: res.data.phone
      })
    }
  } catch (e) {} finally { infoLoading.value = false }
}

async function handleChangePassword() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return

  pwdLoading.value = true
  try {
    const { encrypted: encOld, data: oldPwd } = sm2Encrypt(pwdForm.oldPassword)
    const { encrypted: encNew, data: newPwd } = sm2Encrypt(pwdForm.newPassword)
    const res = await changePassword({
      oldPassword: oldPwd,
      newPassword: newPwd,
      encrypted: encOld && encNew
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
    }
  } catch (e) {} finally { pwdLoading.value = false }
}
</script>

<style scoped>
.page-container { max-width: 1200px; margin: 0 auto; }
.page-header { margin-bottom: 16px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: #1a1a2e; }

.profile-card { border-radius: 12px; border: none; box-shadow: 0 2px 12px rgba(0,0,0,0.04); margin-bottom: 20px; }
.avatar-section { text-align: center; padding: 24px 0 20px; border-bottom: 1px solid #f0f2f5; }
.profile-avatar { background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; font-size: 32px; font-weight: 700; }
.profile-name { font-size: 18px; font-weight: 600; color: #1a1a2e; margin: 12px 0 8px; }
.profile-info-list { padding: 16px 0; }
.profile-info-item { display: flex; align-items: center; gap: 10px; padding: 10px 16px; color: #606266; font-size: 14px; }
.profile-info-item .label { color: #909399; min-width: 60px; }
.profile-info-item .value { color: #303133; flex: 1; text-align: right; }

.edit-card { border-radius: 12px; border: none; box-shadow: 0 2px 12px rgba(0,0,0,0.04); margin-bottom: 20px; }
.edit-form { max-width: 500px; padding-top: 16px; }
.edit-form :deep(.el-input__wrapper) { border-radius: 8px; }
.edit-form :deep(.el-input__wrapper:-webkit-autofill),
.edit-form :deep(input:-webkit-autofill) { -webkit-box-shadow: 0 0 0 1000px white inset !important; transition: background-color 5000s ease-in-out 0s; }
</style>
