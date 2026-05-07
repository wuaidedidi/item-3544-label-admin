<template>
  <div class="page-container">
    <div class="page-header">
      <h2>文件管理</h2>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="文件名">
          <el-input v-model="queryParams.originalName" placeholder="搜索文件名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="action-bar">
        <el-upload
          :show-file-list="false"
          :before-upload="handleBeforeUpload"
          :http-request="handleUpload"
          multiple
          accept="*/*"
        >
          <el-button type="primary" :icon="Upload">上传文件</el-button>
        </el-upload>
        <el-button type="warning" :icon="Download" :disabled="selectedRows.length === 0" @click="handleBatchDownload">批量下载 ({{ selectedRows.length }})</el-button>
        <el-button type="success" :icon="Document" @click="handleExport">导出文件信息Excel</el-button>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="originalName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="fileType" label="文件类型" min-width="140" show-overflow-tooltip />
        <el-table-column label="文件大小" min-width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="uploaderName" label="上传者" min-width="100" />
        <el-table-column prop="createdAt" label="上传时间" min-width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDownload(row)">下载</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Upload, Download, Document } from '@element-plus/icons-vue'
import { getFileList, uploadFile, downloadFile, batchDownloadFiles, deleteFile, exportFiles } from '../api/file'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const selectedRows = ref([])
const queryParams = reactive({ current: 1, size: 10, originalName: '' })

onMounted(() => { loadData() })

async function loadData() {
  loading.value = true
  try {
    const res = await getFileList(queryParams)
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } catch (e) {} finally { loading.value = false }
}

function handleSearch() { queryParams.current = 1; loadData() }
function handleReset() { queryParams.originalName = ''; queryParams.current = 1; loadData() }
function handleSelectionChange(rows) { selectedRows.value = rows }

async function handleBatchDownload() {
  if (selectedRows.value.length === 0) return
  try {
    const ids = selectedRows.value.map(r => r.id)
    const res = await batchDownloadFiles(ids)
    const blob = new Blob([res], { type: 'application/zip' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url; a.download = '批量下载文件.zip'; a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success(`已下载 ${ids.length} 个文件`)
  } catch (e) {}
}

function handleBeforeUpload(file) {
  if (file.size > 50 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过50MB')
    return false
  }
  return true
}

async function handleUpload({ file }) {
  try {
    const res = await uploadFile(file)
    if (res.code === 200) { ElMessage.success('上传成功'); loadData() }
  } catch (e) {}
}

async function handleDownload(row) {
  try {
    const res = await downloadFile(row.id)
    const blob = new Blob([res])
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = row.originalName
    a.click()
    window.URL.revokeObjectURL(url)
  } catch (e) {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除文件 "${row.originalName}" 吗？`, '确认删除', { type: 'warning' })
    const res = await deleteFile(row.id)
    if (res.code === 200) { ElMessage.success('删除成功'); loadData() }
  } catch (e) {}
}

async function handleExport() {
  try {
    const res = await exportFiles({ originalName: queryParams.originalName })
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url; a.download = '文件列表.xlsx'; a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {}
}

function formatFileSize(bytes) {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
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
</style>
