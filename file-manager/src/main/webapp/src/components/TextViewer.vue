<template>
  <div class="text-viewer">
    <!-- 工具栏 -->
    <div class="text-toolbar">
      <el-button-group v-if="format === 'md'">
        <el-button
          size="small"
          :type="viewMode === 'edit' ? 'primary' : 'default'"
          @click="viewMode = 'edit'"
        >
          编辑
        </el-button>
        <el-button
          size="small"
          :type="viewMode === 'preview' ? 'primary' : 'default'"
          @click="viewMode = 'preview'"
        >
          预览
        </el-button>
      </el-button-group>
      <span class="encoding">UTF-8</span>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-loading">
      <el-progress :percentage="loadProgress" :status="loadStatus" />
      <p>正在加载: {{ loadProgress }}%</p>
    </div>

    <!-- 内容 -->
    <div v-else ref="contentContainer" class="text-content">
      <!-- Markdown 编辑/预览模式 -->
      <div v-if="format === 'md'" class="md-mode">
        <textarea
          v-if="viewMode === 'edit'"
          v-model="content"
          class="md-editor"
          readonly
        />
        <div
          v-else
          class="md-preview"
          v-html="renderedContent"
        />
      </div>

      <!-- 纯文本模式 -->
      <pre v-else class="plain-text">{{ content }}</pre>
    </div>

    <!-- 大文件提示 -->
    <div v-if="isLargeFile" class="large-file-tips">
      <el-alert type="info" :closable="false">
        大文件已加载，显示前 {{ truncateLines }} 行
        <el-button type="text" @click="loadFullContent">加载完整内容</el-button>
      </el-alert>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getTextPreview } from '../api/preview'

const props = defineProps({
  url: {
    type: String,
    default: ''
  },
  fileId: {
    type: Number,
    required: true
  },
  format: {
    type: String,
    default: 'txt'
  }
})

// 状态
const loading = ref(true)
const loadProgress = ref(0)
const loadStatus = ref('')
const content = ref('')
const viewMode = ref('preview')
const isLargeFile = ref(false)
const truncateLines = ref(500)
const contentContainer = ref(null)

// 大文件阈值
const LARGE_FILE_SIZE = 20 * 1024 * 1024
const MAX_LINES = 500

// 渲染的 Markdown 内容
const renderedContent = computed(() => {
  if (props.format !== 'md') return ''
  // 简单渲染：标题和段落
  return content.value
    .split('\n')
    .map(line => {
      if (line.startsWith('# ')) {
        return `<h1>${line.slice(2)}</h1>`
      }
      if (line.startsWith('## ')) {
        return `<h2>${line.slice(3)}</h2>`
      }
      if (line.startsWith('### ')) {
        return `<h3>${line.slice(4)}</h3>`
      }
      if (line.startsWith('- ')) {
        return `<li>${line.slice(2)}</li>`
      }
      if (line.startsWith('```')) {
        return `<pre>${line}</pre>`
      }
      return `<p>${line}</p>`
    })
    .join('')
})

// 加载文本内容
const loadText = async () => {
  loading.value = true
  loadProgress.value = 0

  try {
    const res = await getTextPreview(props.fileId)
    if (res.code === 0) {
      let text = res.data || ''
      // 检查是否大文件
      if (text.length > LARGE_FILE_SIZE) {
        isLargeFile.value = true
        // 截断显示
        const lines = text.split('\n')
        if (lines.length > MAX_LINES) {
          text = lines.slice(0, MAX_LINES).join('\n')
          truncateLines.value = MAX_LINES
        }
      }
      content.value = text
    } else {
      loadStatus.value = 'exception'
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    loadStatus.value = 'exception'
    ElMessage.error('加载失败: ' + e.message)
  } finally {
    loading.value = false
  }
}

// 加载完整内容
const loadFullContent = async () => {
  truncateLines.value = -1
  isLargeFile.value = false
  await loadText()
}

// 根据 URL 加载
const loadFromUrl = async () => {
  if (!props.url) return

  loading.value = true
  try {
    const response = await fetch(props.url)
    const text = await response.text()

    // 检查是否大文件
    if (text.length > LARGE_FILE_SIZE) {
      isLargeFile.value = true
      const lines = text.split('\n')
      if (lines.length > MAX_LINES) {
        content.value = lines.slice(0, MAX_LINES).join('\n')
        truncateLines.value = MAX_LINES
        return
      }
    }
    content.value = text
  } catch (e) {
    ElMessage.error('加载失败: ' + e.message)
  } finally {
    loading.value = false
  }
}

// 初始加载
onMounted(() => {
  if (props.fileId) {
    loadText()
  } else if (props.url) {
    loadFromUrl()
  }
})

// 监听 fileId 变化
watch(() => props.fileId, () => {
  loadText()
})
</script>

<style scoped>
.text-viewer {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.text-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-bottom: 1px solid #eee;
  background: #f5f7fa;
}

.encoding {
  color: #909399;
  font-size: 12px;
}

.text-loading {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.text-content {
  flex: 1;
  overflow: auto;
  padding: 20px;
  background: #fff;
}

.md-mode {
  width: 100%;
  height: 100%;
}

.md-editor {
  width: 100%;
  height: 100%;
  border: none;
  resize: none;
  font-family: monospace;
  font-size: 14px;
  line-height: 1.6;
  padding: 0;
  outline: none;
}

.md-preview {
  line-height: 1.8;
}

.md-preview :deep(h1) {
  font-size: 24px;
  font-weight: 600;
  margin: 16px 0;
}

.md-preview :deep(h2) {
  font-size: 20px;
  font-weight: 600;
  margin: 14px 0;
}

.md-preview :deep(h3) {
  font-size: 16px;
  font-weight: 600;
  margin: 12px 0;
}

.md-preview :deep(p) {
  margin: 8px 0;
}

.md-preview :deep(li) {
  margin-left: 20px;
  list-style: disc;
}

.md-preview :deep(pre) {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  overflow: auto;
}

.plain-text {
  margin: 0;
  font-family: monospace;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.large-file-tips {
  padding: 10px;
  border-top: 1px solid #eee;
}
</style>