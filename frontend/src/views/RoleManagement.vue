<template>
  <div class="page-container">
    <div class="page-header">
      <h2>角色管理</h2>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="角色名称">
          <el-input v-model="queryParams.roleName" placeholder="搜索角色名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="action-bar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增角色</el-button>
        <el-button type="success" :icon="Download" @click="handleExport">导出Excel</el-button>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" min-width="120" />
        <el-table-column prop="roleCode" label="角色编码" min-width="120" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
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
              :disabled="row.roleCode === 'ADMIN'">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码 (如: EDITOR)" :disabled="isEdit && form.roleCode === 'ADMIN'" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="权限分配">
          <div class="perm-tree-wrap">
            <el-tree
              ref="treeRef"
              :data="permTree"
              show-checkbox
              node-key="id"
              :default-checked-keys="form.permissionIds"
              :props="{ label: 'name', children: 'children' }"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Download } from '@element-plus/icons-vue'
import { getRoleList, createRole, updateRole, deleteRole, getAllPermissions, getRolePermissions, exportRoles } from '../api/role'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const treeRef = ref(null)
const permTree = ref([])

const queryParams = reactive({ current: 1, size: 10, roleName: '' })
const form = ref({ id: null, roleName: '', roleCode: '', description: '', status: 1, permissionIds: [] })

const formRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

onMounted(() => {
  loadData()
  loadPermissions()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getRoleList(queryParams)
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } catch (e) {} finally { loading.value = false }
}

async function loadPermissions() {
  try {
    const res = await getAllPermissions()
    if (res.code === 200) {
      const perms = res.data
      const map = {}
      const tree = []
      perms.forEach(p => { map[p.id] = { ...p, children: [] } })
      perms.forEach(p => {
        if (p.parentId === 0) { tree.push(map[p.id]) }
        else if (map[p.parentId]) { map[p.parentId].children.push(map[p.id]) }
      })
      permTree.value = tree
    }
  } catch (e) {}
}

function handleSearch() { queryParams.current = 1; loadData() }
function handleReset() { queryParams.roleName = ''; queryParams.current = 1; loadData() }

function handleAdd() {
  isEdit.value = false
  form.value = { id: null, roleName: '', roleCode: '', description: '', status: 1, permissionIds: [] }
  dialogVisible.value = true
}

async function handleEdit(row) {
  isEdit.value = true
  form.value = { id: row.id, roleName: row.roleName, roleCode: row.roleCode, description: row.description || '', status: row.status, permissionIds: [] }
  try {
    const res = await getRolePermissions(row.id)
    if (res.code === 200) {
      form.value.permissionIds = res.data
      dialogVisible.value = true
    }
  } catch (e) { dialogVisible.value = true }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const checkedKeys = treeRef.value.getCheckedKeys()
  const halfCheckedKeys = treeRef.value.getHalfCheckedKeys()
  const allKeys = [...checkedKeys, ...halfCheckedKeys]

  submitLoading.value = true
  try {
    const data = { ...form.value, permissionIds: allKeys }
    if (isEdit.value) {
      const res = await updateRole(data)
      if (res.code === 200) { ElMessage.success('更新角色成功'); dialogVisible.value = false; loadData() }
    } else {
      const res = await createRole(data)
      if (res.code === 200) { ElMessage.success('创建角色成功'); dialogVisible.value = false; loadData() }
    }
  } catch (e) {} finally { submitLoading.value = false }
}

async function handleExport() {
  try {
    const res = await exportRoles({ roleName: queryParams.roleName })
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url; a.download = '角色列表.xlsx'; a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗？`, '确认删除', { type: 'warning' })
    const res = await deleteRole(row.id)
    if (res.code === 200) { ElMessage.success('删除角色成功'); loadData() }
  } catch (e) {}
}
</script>

<style scoped>
.page-container { max-width: 1400px; margin: 0 auto; }
.page-header { margin-bottom: 16px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: #1a1a2e; }
.filter-card { border-radius: 12px; border: none; margin-bottom: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.04); }
.filter-form { display: flex; flex-wrap: wrap; }
.action-bar { display: flex; gap: 8px; margin-top: 8px; }
.table-card { border-radius: 12px; border: none; box-shadow: 0 2px 12px rgba(0,0,0,0.04); }
.pagination-wrap { display: flex; justify-content: flex-end; padding: 16px 0 0; }
.perm-tree-wrap { max-height: 300px; overflow-y: auto; border: 1px solid #ebeef5; border-radius: 8px; padding: 12px; width: 100%; }
</style>
