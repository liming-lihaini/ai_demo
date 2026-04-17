<template>
  <el-dialog
    v-model="dialogVisible"
    :title="fileName"
    :width="dialogWidth"
    :fullscreen="isFullscreen"
    :close-on-click-modal="false"
    @close="handleClose"
    class="preview-dialog"
  >
    <template #header>
      <div class="preview-dialog-header">
        <span class="preview-title">{{ fileName }}</span>
        <div class="preview-actions">
          <el-button
            :icon="isFullscreen ? 'CloseBold' : 'FullScreen'"
            circle
            size="small"
            @click="toggleFullscreen"
          />
          <el-button
            icon="Download"
            circle
            size="small"
            @click="handleDownload"
          />
        </div>
      </div>
    </template>

    <div class="preview-content">
      <!-- Loading -->
      <div v-if="loading" class="preview-loading">
        <el-icon class="is-loading" :size="40">
          <Loading />
        </el-icon>
        <p>加载中...</p>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="preview-error">
        <el-result icon="error" :title="error">
          <template #sub-title>
            <el-button type="primary" @click="handleDownload">下载到本地查看</el-button>
          </template>
        </el-result>
      </div>

      <!-- Content -->
      <div v-else class="preview-viewer">
        <PdfViewer
          v-if="previewType === 'pdf'"
          :url="contentUrl"
          :file-id="fileId"
        />
        <TextViewer
          v-else-if="previewType === 'text'"
          :url="contentUrl"
          :file-id="fileId"
          :format="format"
        />
        <ImageViewer
          v-else-if="previewType === 'image'"
          :images="[contentUrl]"
          :initial-index="0"
        />
        <MediaPlayer
          v-else-if="previewType === 'audio'"
          :url="contentUrl"
          :file-id="fileId"
          type="audio"
        />
        <MediaPlayer
          v-else-if="previewType === 'video'"
          :url="contentUrl"
          :file-id="fileId"
          type="video"
        />
        <TextViewer
          v-else-if="previewType === 'office'"
          :url="contentUrl"
          :file-id="fileId"
          format="html"
        />
        <div v-else class="preview-unsupported">
          <el-result icon="info" title="暂不支持预览">
            <template #sub-title>
              <p>该文件类型暂不支持在线预览</p>
              <el-button type="primary" @click="handleDownload">下载到本地查看</el-button>
            </template>
          </el-result>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Loading, FullScreen, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PdfViewer from './PdfViewer.vue'
import TextViewer from './TextViewer.vue'
import ImageViewer from './ImageViewer.vue'
import MediaPlayer from './MediaPlayer.vue'
import { getPreviewInfo, getBlobUrl } from '../api/preview'
import * as fileApi from '../api/file'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  fileId: {
    type: Number,
    required: true
  },
  fileName: {
    type: String,
    default: ''
  },
  fileType: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:visible', 'fullscreen', 'download', 'close'])

// 对话框状态
const dialogVisible = ref(false)
const isFullscreen = ref(false)
const loading = ref(true)
const error = ref('')
const previewType = ref('')
const contentUrl = ref('')
const format = ref('')

// 监听 visible 变化
watch(() => props.visible, (val) => {
  dialogVisible.value = val
  if (val) {
    loadPreviewInfo()
  }
})

// 监听对话框关闭
watch(dialogVisible, (val) => {
  if (!val) {
    emit('update:visible', false)
    emit('close')
  }
})

// 对话框宽度
const dialogWidth = computed(() => {
  switch (previewType.value) {
    case 'image':
      return '80%'
    case 'video':
      return '80%'
    case 'audio':
      return '500px'
    default:
      return '80%'
  }
})

// 加载预览信息
const loadPreviewInfo = async () => {
  loading.value = true
  error.value = ''

  try {
    const res = await getPreviewInfo(props.fileId)
    if (res.code === 0) {
      const info = res.data
      previewType.value = info.previewType || 'unsupported'
      format.value = info.format || ''

      // 获取内容 URL
      contentUrl.value = getBlobUrl(props.fileId)
    } else {
      error.value = res.message || '获取预览信息失败'
    }
  } catch (e) {
    error.value = e.message || '加载预览失败'
  } finally {
    loading.value = false
  }
}

// 切换全屏
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
  emit('fullscreen', isFullscreen.value)
}

// 下载
const handleDownload = async () => {
  try {
    const url = fileApi.getDownloadUrl(props.fileId)
    const link = document.createElement('a')
    link.href = url
    link.download = props.fileName
    link.click()
    ElMessage.success('开始下载')
    emit('download')
  } catch (e) {
    ElMessage.error('下载失败')
  }
}

// 关闭
const handleClose = () => {
  dialogVisible.value = false
}
</script>

<style scoped>
.preview-dialog :deep(.el-dialog__header) {
  padding: 10px 20px;
  border-bottom: 1px solid #eee;
}

.preview-dialog :deep(.el-dialog__body) {
  padding: 0;
  height: calc(100vh - 150px);
  overflow: auto;
}

.preview-dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.preview-title {
  font-size: 16px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-actions {
  display: flex;
  gap: 10px;
}

.preview-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-loading {
  text-align: center;
  color: #909399;
}

.preview-loading p {
  margin-top: 10px;
}

.preview-error {
  width: 100%;
  padding: 20px;
}

.preview-viewer {
  width: 100%;
  height: 100%;
}

.preview-unsupported {
  width: 100%;
  padding: 20px;
}
</style>