<template>
  <div class="files-view">
    <div class="toolbar">
      <el-button type="primary" @click="handleCreateDir">
        <el-icon><FolderAdd /></el-icon>
        新建目录
      </el-button>
      <el-upload
        class="upload-wrapper"
        :action="uploadUrl"
        :data="uploadData"
        :show-file-list="false"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
        multiple
      >
        <el-button type="primary">
          <el-icon><Upload /></el-icon>
          上传文件
        </el-button>
      </el-upload>
      <el-input
        v-model="searchKeyword"
        placeholder="搜索文件..."
        style="width: 200px; margin-left: 10px;"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="handleSearch" />
        </template>
      </el-input>
    </div>

    <el-table
      :data="fileList"
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="name" label="名称" min-width="200">
        <template #default="{ row }">
          <div class="file-name">
            <span class="file-icon">{{ getFileIcon(row.format) }}</span>
            {{ row.name }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="size" label="大小" width="120">
        <template #default="{ row }">
          {{ formatSize(row.size) }}
        </template>
      </el-table-column>
      <el-table-column prop="format" label="格式" width="80" />
      <el-table-column prop="createdAt" label="上传时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handlePreview(row)">预览</el-button>
          <el-button link type="primary" @click="handleDownload(row)">下载</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 批量操作栏 -->
    <div v-if="selectedFiles.length > 0" class="batch-actions">
      <span>已选择 {{ selectedFiles.length }} 项</span>
      <el-button type="danger" size="small" @click="handleBatchDelete">批量删除</el-button>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="fileList.length === 0" description="暂无文件" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FolderAdd, Upload, Search } from '@element-plus/icons-vue'
import * as fileApi from '../api/file'

const fileList = ref([])
const currentDir = ref(null)
const searchKeyword = ref('')
const selectedFiles = ref([])

const userId = ref(1)
const uploadUrl = computed(() => `${window.location.origin}/api/file/upload`)
const uploadData = ref({
  userId: userId.value,
  parentId: currentDir.value || 0
})

// 格式化文件大小
const formatSize = (bytes) => {
  if (!bytes || bytes < 0) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / 1024 / 1024).toFixed(1) + ' MB'
  return (bytes / 1024 / 1024 / 1024).toFixed(1) + ' GB'
}

// 获取文件图标
const getFileIcon = (format) => {
  if (!format) return '📄'
  const ext = format.toLowerCase()

  const icons = {
    pdf: '📕',
    doc: '📘', docx: '📘',
    xls: '📗', xlsx: '📗',
    txt: '📄',
    md: '📝',
    jpg: '🖼️', jpeg: '🖼️', png: '🖼️', gif: '🖼️', bmp: '🖼️',
    mp3: '🎵', wav: '🎵',
    mp4: '🎬', avi: '🎬'
  }

  return icons[ext] || '📄'
}

// 加载文件列表
const loadFiles = async () => {
  try {
    const res = await fileApi.getFileList(currentDir.value || 0, userId.value)
    if (res.code === 0) {
      fileList.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error('加载文件列表失败')
  }
}

// 上传前检查
const beforeUpload = (file) => {
  const maxSize = 100 * 1024 * 1024 // 100MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过100MB')
    return false
  }
  uploadData.value = { userId: userId.value, parentId: currentDir.value || 0 }
  return true
}

// 上传成功
const handleUploadSuccess = (response) => {
  if (response.code === 0) {
    ElMessage.success('上传成功')
    loadFiles()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败
const handleUploadError = (error) => {
  ElMessage.error('上传失败: ' + error.message)
}

// 搜索
const handleSearch = async () => {
  if (!searchKeyword.value) {
    loadFiles()
    return
  }
  try {
    const res = await fileApi.searchFiles(searchKeyword.value, userId.value)
    if (res.code === 0) {
      fileList.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('搜索失败')
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedFiles.value = selection
}

// 创建目录（待实现）
const handleCreateDir = () => {
  ElMessage.info('请使用目录管理页面创建目录')
}

// 预览（待实现）
const handlePreview = (row) => {
  ElMessage.info('预览功能待实现')
}

// 下载
const handleDownload = async (row) => {
  try {
    const url = fileApi.getDownloadUrl(row.id)
    const link = document.createElement('a')
    link.href = url
    link.download = row.name
    link.click()
    ElMessage.success('开始下载')
  } catch (e) {
    ElMessage.error('下载失败')
  }
}

// 删除单个
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 ${row.name} 吗？删除后文件将移入回收站`,
      '确认删除',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await fileApi.deleteFile(row.id)
    if (res.code === 0 || res.code === undefined) {
      ElMessage.success('删除成功')
      loadFiles()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedFiles.value.length === 0) return

  try {
    const ids = selectedFiles.value.map(f => f.id)
    await ElMessageBox.confirm(
      `确定要删除选中的 ${ids.length} 个文件吗？`,
      '确认批量删除',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await fileApi.deleteFiles(ids)
    if (res.code === 0 || res.code === undefined) {
      ElMessage.success('删除成功')
      loadFiles()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
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
  display: flex;
  align-items: center;
}

.toolbar .el-button {
  margin-right: 10px;
}

.upload-wrapper {
  display: inline-block;
}

.file-name {
  display: flex;
  align-items: center;
}

.file-icon {
  margin-right: 8px;
  font-size: 18px;
}

.batch-actions {
  margin-top: 20px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>