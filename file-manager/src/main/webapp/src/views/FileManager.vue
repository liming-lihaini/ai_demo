<template>
  <div class="file-manager">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-upload
        class="upload-demo"
        :action="uploadUrl"
        :data="uploadParams"
        :show-file-list="false"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :on-progress="handleUploadProgress"
        :before-upload="beforeUpload"
        multiple
      >
        <el-button type="primary">上传文件</el-button>
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

    <!-- 文件列表 -->
    <el-table
      :data="fileList"
      @selection-change="handleSelectionChange"
      style="width: 100%; margin-top: 20px;"
      @row-dblclick="handleRowDblClick"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="文件名" min-width="200">
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
      <el-table-column prop="format" label="类型" width="80" />
      <el-table-column prop="createdAt" label="上传时间" width="180" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="text" @click.stop="handleDownload(row)">下载</el-button>
          <el-button type="text" @click.stop="handleRename(row)">重命名</el-button>
          <el-button type="text" @click.stop="handleDelete(row)" style="color: #f56c6c;">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="fileList.length === 0" description="暂无文件" />

    <!-- 批量操作栏 -->
    <div v-if="selectedFiles.length > 0" class="batch-actions">
      <span>已选择 {{ selectedFiles.length }} 项</span>
      <el-button type="danger" size="small" @click="handleBatchDelete">批量删除</el-button>
    </div>

    <!-- 上传进度对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传文件" width="400px">
      <el-progress :percentage="uploadPercentage" :status="uploadStatus" />
      <p>正在上传: {{ uploadingFileName }}</p>
    </el-dialog>

    <!-- 重命名对话框 -->
    <el-dialog v-model="renameDialogVisible" title="重命名" width="400px">
      <el-input v-model="newFileName" placeholder="请输入新名称" />
      <template #footer>
        <el-button @click="renameDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRename">确定</el-button>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="400px">
      <p>确定要删除选中的 {{ deleteCount }} 个文件吗？</p>
      <p style="color: #e6a23c;">删除后文件将移入回收站，30天后自动清除</p>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete">确认删除</el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <PreviewDialog
      v-if="previewFile"
      v-model:visible="previewVisible"
      :file-id="previewFile.id"
      :file-name="previewFile.name"
      :file-type="getFileType(previewFile.format)"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, Delete, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as fileApi from '../api/file'
import PreviewDialog from '../components/PreviewDialog.vue'

const userId = ref(1)
const parentId = ref(0)
const fileList = ref([])
const searchKeyword = ref('')
const selectedFiles = ref([])
const selectedIds = ref([])

// 预览相关
const previewVisible = ref(false)
const previewFile = ref(null)

// 对话框状态
const uploadDialogVisible = ref(false)
const uploadPercentage = ref(0)
const uploadStatus = ref('')
const uploadingFileName = ref('')

const renameDialogVisible = ref(false)
const renameId = ref(null)
const newFileName = ref('')

const deleteDialogVisible = ref(false)
const deleteCount = ref(0)

const uploadUrl = ref('')
const uploadParams = ref({})

// 初始化上传URL
const initUpload = () => {
  const base = window.location.origin
  uploadUrl.value = `${base}/api/file/upload`
  uploadParams.value = { userId: userId.value, parentId: parentId.value }
}

// 加载文件列表
const loadFileList = async () => {
  try {
    const res = await fileApi.getFileList(parentId.value, userId.value)
    if (res.code === 0) {
      fileList.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
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
  uploadingFileName.value = file.name
  uploadDialogVisible.value = true
  uploadPercentage.value = 0
  return true
}

// 上传成功
const handleUploadSuccess = (response, file, fileList) => {
  uploadPercentage.value = 100
  uploadStatus = 'success'
  setTimeout(() => {
    uploadDialogVisible.value = false
    loadFileList()
  }, 500)
  ElMessage.success('上传成功')
}

// 上传失败
const handleUploadError = (error) => {
  uploadStatus = 'exception'
  ElMessage.error('上传失败: ' + error.message)
}

// 上传进度
const handleUploadProgress = (event, file, fileList) => {
  uploadPercentage.value = Math.round(event.percent)
}

// 搜索
const handleSearch = async () => {
  if (!searchKeyword.value) {
    loadFileList()
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
  selectedIds.value = selection.map(f => f.id)
}

// 行双击打开预览
const handleRowDblClick = (row) => {
  previewFile.value = row
  previewVisible.value = true
}

// 判断文件是否支持预览
const isPreviewable = (format) => {
  if (!format) return false
  const previewFormats = ['pdf', 'txt', 'md', 'jpg', 'jpeg', 'png', 'gif', 'bmp', 'mp3', 'wav', 'mp4', 'avi', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx']
  return previewFormats.includes(format.toLowerCase())
}

// 获取文件类型
const getFileType = (format) => {
  if (!format) return ''
  const ext = format.toLowerCase()
  if (['pdf'].includes(ext)) return 'pdf'
  if (['txt', 'md'].includes(ext)) return 'text'
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp'].includes(ext)) return 'image'
  if (['mp3', 'wav'].includes(ext)) return 'audio'
  if (['mp4', 'avi'].includes(ext)) return 'video'
  if (['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'].includes(ext)) return 'office'
  return ''
}

// 下载
const handleDownload = async (file) => {
  try {
    const url = fileApi.getDownloadUrl(file.id)
    const link = document.createElement('a')
    link.href = url
    link.download = file.name
    link.click()
    ElMessage.success('开始下载')
  } catch (e) {
    ElMessage.error('下载失败')
  }
}

// 重命名
const handleRename = (file) => {
  renameId.value = file.id
  newFileName.value = file.name
  renameDialogVisible.value = true
}

const confirmRename = async () => {
  if (!newFileName.value) {
    ElMessage.warning('请输入文件名')
    return
  }
  try {
    const res = await fileApi.renameFile(renameId.value, newFileName.value)
    if (res.code === 0) {
      ElMessage.success('重命名成功')
      renameDialogVisible.value = false
      loadFileList()
    } else {
      ElMessage.error(res.message || '重命名失败')
    }
  } catch (e) {
    ElMessage.error('重命名失败')
  }
}

// 删除单个
const handleDelete = (file) => {
  deleteCount.value = 1
  selectedIds.value = [file.id]
  deleteDialogVisible.value = true
}

// 批量删除
const handleBatchDelete = () => {
  deleteCount.value = selectedFiles.value.length
  deleteDialogVisible.value = true
}

const confirmDelete = async () => {
  try {
    const res = await fileApi.deleteFiles(selectedIds.value)
    if (res.code === 0 || res.code === undefined) {
      ElMessage.success('删除成功')
      deleteDialogVisible.value = false
      loadFileList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
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

// 格式化文件大小
const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return `${size.toFixed(1)} ${units[unitIndex]}`
}

onMounted(() => {
  initUpload()
  loadFileList()
})
</script>

<style scoped>
.file-manager {
  padding: 20px;
}

.toolbar {
  display: flex;
  align-items: center;
}

.file-name {
  display: flex;
  align-items: center;
}

.file-icon {
  margin-right: 8px;
  font-size: 20px;
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