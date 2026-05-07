<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="搜索用户名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="action-bar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
        <el-button type="success" :icon="Download" @click="handleExport">导出Excel</el-button>
        <el-button type="warning" :icon="Upload" @click="importDialogVisible = true">导入Excel</el-button>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%" row-key="id">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="roleName" label="角色" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.roleCode === 'ADMIN' ? 'danger' : 'info'" size="small">
              {{ row.roleName || '未分配' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)"
              :disabled="row.id === currentUserId">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱 (选填)" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号 (选填)" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%"
            :disabled="isEdit && form.id === currentUserId && currentRoleCode === 'ADMIN'">
            <el-option v-for="role in roleList" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%"
            :disabled="isEdit && form.id === currentUserId">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入用户" width="480px">
      <div class="import-tips">
        <p>请上传Excel文件 (.xlsx)，包含以下列：用户名、昵称、邮箱、手机号</p>
        <p>导入的用户默认密码为 <strong>123456</strong>，默认角色为 <strong>普通用户</strong></p>
      </div>
      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
      >
        <el-icon :size="48" class="upload-icon"><UploadFilled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="handleImport">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Download, Upload, UploadFilled } from '@element-plus/icons-vue'
import { getUserList, createUser, updateUser, deleteUser, exportUsers, importUsers } from '../api/user'
import { getAllRoles } from '../api/role'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const currentUserId = computed(() => userStore.userInfo?.id)
const currentRoleCode = computed(() => userStore.userInfo?.roleCode)

const loading = ref(false)
const submitLoading = ref(false)
const importLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const roleList = ref([])
const dialogVisible = ref(false)
const importDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const uploadRef = ref(null)
const importFile = ref(null)

const queryParams = reactive({ current: 1, size: 10, username: '', status: null })

const form = ref({
  id: null, username: '', password: '', nickname: '', email: '', phone: '', roleId: null, status: 1
})

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

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, max: 50, message: '用户名长度为3-50个字符', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码长度不少于6位', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }],
  email: [{ validator: validateEmail, trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }]
}

onMounted(() => {
  loadData()
  loadRoles()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {} finally { loading.value = false }
}

async function loadRoles() {
  try {
    const res = await getAllRoles()
    if (res.code === 200) { roleList.value = res.data }
  } catch (e) {}
}

function handleSearch() { queryParams.current = 1; loadData() }
function handleReset() { queryParams.username = ''; queryParams.status = null; queryParams.current = 1; loadData() }

function handleAdd() {
  isEdit.value = false
  form.value = { id: null, username: '', password: '', nickname: '', email: '', phone: '', roleId: 2, status: 1 }
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  form.value = { id: row.id, username: row.username, nickname: row.nickname || '', email: row.email || '', phone: row.phone || '', roleId: row.roleId, status: row.status }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      const res = await updateUser(form.value)
      if (res.code === 200) { ElMessage.success('更新用户成功'); dialogVisible.value = false; loadData() }
    } else {
      const res = await createUser(form.value)
      if (res.code === 200) { ElMessage.success('创建用户成功'); dialogVisible.value = false; loadData() }
    }
  } catch (e) {} finally { submitLoading.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '确认删除', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
    const res = await deleteUser(row.id)
    if (res.code === 200) { ElMessage.success('删除用户成功'); loadData() }
  } catch (e) {}
}

async function handleExport() {
  try {
    const res = await exportUsers({ username: queryParams.username, status: queryParams.status })
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url; a.download = '用户列表.xlsx'; a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {}
}

function handleFileChange(file) { importFile.value = file.raw }

async function handleImport() {
  if (!importFile.value) { ElMessage.warning('请选择要导入的文件'); return }
  importLoading.value = true
  try {
    const res = await importUsers(importFile.value)
    if (res.code === 200) { ElMessage.success(res.message || '导入成功'); importDialogVisible.value = false; importFile.value = null; loadData() }
  } catch (e) {} finally { importLoading.value = false }
}
</script>

<style scoped>
.page-container { max-width: 1400px; margin: 0 auto; }
.page-header { margin-bottom: 16px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: #1a1a2e; }
.filter-card { border-radius: 12px; border: none; margin-bottom: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.04); }
.filter-form { display: flex; flex-wrap: wrap; gap: 0; }
.action-bar { display: flex; gap: 8px; margin-top: 8px; }
.table-card { border-radius: 12px; border: none; box-shadow: 0 2px 12px rgba(0,0,0,0.04); }
.pagination-wrap { display: flex; justify-content: flex-end; padding: 16px 0 0; }
.import-tips { background: #f0f9eb; border-radius: 8px; padding: 12px 16px; margin-bottom: 16px; font-size: 13px; color: #67C23A; line-height: 1.8; }
.upload-icon { color: #c0c4cc; }
</style>
