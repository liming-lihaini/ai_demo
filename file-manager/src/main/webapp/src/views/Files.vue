<template>
  <div class="files-view">
    <div class="toolbar">
      <el-button type="primary" @click="handleCreateDir">
        <el-icon><FolderAdd /></el-icon>
        新建目录
      </el-button>
      <el-upload
        :action="uploadUrl"
        :data="uploadData"
        :show-file-list="false"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        multiple
      >
        <el-button type="primary">
          <el-icon><Upload /></el-icon>
          上传文件
        </el-button>
      </el-upload>
    </div>
    <el-table :data="fileList" style="width: 100%">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="format" label="格式" width="80" />
      <el-table-column prop="size" label="大小" width="120">
        <template #default="{ row }">
          {{ formatSize(row.size) }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="上传时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="handlePreview(row)">预览</el-button>
          <el-button link type="primary" @click="handleDownload(row)">下载</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { FolderAdd, Upload } from '@element-plus/icons-vue'
import request from '../api/request'

const fileList = ref([])
const currentDir = ref(null)
const uploadUrl = '/api/files/upload'

const uploadData = ref({
  dirId: null
})

const formatSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / 1024 / 1024).toFixed(1) + ' MB'
  return (bytes / 1024 / 1024 / 1024).toFixed(1) + ' GB'
}

const loadFiles = async () => {
  try {
    const data = await request.get('/files', { params: { dirId: currentDir.value } })
    fileList.value = data
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleCreateDir = () => {
  // TODO: 创建目录对话框
}

const handleUploadSuccess = () => {
  ElMessage.success('上传成功')
  loadFiles()
}

const handleUploadError = (error) => {
  ElMessage.error(error.message)
}

const handlePreview = (row) => {
  // TODO: 预览
}

const handleDownload = (row) => {
  // TODO: 下载
}

const handleDelete = async (row) => {
  try {
    await request.delete(`/files/${row.id}`)
    ElMessage.success('删除成功')
    loadFiles()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(() => {
  loadFiles()
})
</script>

<style scoped>
.files-view {
  padding: 20px;
}
.toolbar {
  margin-bottom: 20px;
}
.toolbar .el-button {
  margin-right: 10px;
}
</style>