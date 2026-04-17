<template>
  <div class="pdf-viewer">
    <!-- 工具栏 -->
    <div class="pdf-toolbar">
      <el-button-group>
        <el-button :icon="ZoomOut" size="small" @click="zoomOut" />
        <el-button :icon="ZoomIn" size="small" @click="zoomIn" />
      </el-button-group>
      <span class="zoom-level">{{ Math.round(scale * 100) }}%</span>
      <el-button-group>
        <el-button size="small" :disabled="currentPage <= 1" @click="prevPage">上一页</el-button>
        <el-button size="small" :disabled="currentPage >= totalPages" @click="nextPage">下一页</el-button>
      </el-button-group>
      <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="pdf-loading">
      <el-progress :percentage="loadProgress" :status="loadStatus" />
      <p>正在加载 PDF: {{ loadProgress }}%</p>
    </div>

    <!-- Canvas -->
    <div v-else ref="viewerContainer" class="pdf-container">
      <canvas ref="pdfCanvas" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { ZoomIn, ZoomOut } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as pdfjsLib from 'pdfjs-dist'

// 设置 worker
pdfjsLib.GlobalWorkerOptions.workerSrc = new URL(
  'pdfjs-dist/build/pdf.worker.mjs',
  import.meta.url
).href

const props = defineProps({
  url: {
    type: String,
    required: true
  },
  fileId: {
    type: Number,
    required: true
  }
})

// 状态
const loading = ref(true)
const loadProgress = ref(0)
const loadStatus = ref('')
const pdfDoc = ref(null)
const currentPage = ref(1)
const totalPages = ref(0)
const scale = ref(1.5)
const viewerContainer = ref(null)
const pdfCanvas = ref(null)

// 大文件阈值: 20MB
const LARGE_FILE_SIZE = 20 * 1024 * 1024

// 加载 PDF
const loadPdf = async () => {
  loading.value = true
  loadProgress.value = 0

  try {
    // 加载文档
    const loadingTask = pdfjsLib.getDocument({
      url: props.url,
      cMapUrl: 'https://cdn.jsdelivr.net/npm/pdfjs-dist@4.0.379/cmaps/',
      cMapPacked: true,
      // 大文件使用-range request
      disableRange: false,
      disableStream: false
    })

    // 进度回调
    loadingTask.onProgress = (progress) => {
      if (progress.total) {
        loadProgress.value = Math.round((progress.loaded / progress.total) * 100)
      }
    }

    pdfDoc.value = await loadingTask.promise
    totalPages.value = pdfDoc.value.numPages

    // 渲染第一页
    renderPage(currentPage.value)
  } catch (e) {
    loadStatus.value = 'exception'
    ElMessage.error('加载 PDF 失败: ' + e.message)
  } finally {
    loading.value = false
  }
}

// 渲染页面
const renderPage = async (pageNum) => {
  if (!pdfDoc.value || !pdfCanvas.value) return

  try {
    const page = await pdfDoc.value.getPage(pageNum)
    const viewport = page.getViewport({ scale: scale.value })

    const canvas = pdfCanvas.value
    const context = canvas.getContext('2d')

    canvas.height = viewport.height
    canvas.width = viewport.width

    const renderContext = {
      canvasContext: context,
      viewport: viewport
    }

    await page.render(renderContext).promise
  } catch (e) {
    ElMessage.error('渲染页面失败: ' + e.message)
  }
}

// 上一页
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    renderPage(currentPage.value)
  }
}

// 下一页
const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    renderPage(currentPage.value)
  }
}

// 放大
const zoomIn = () => {
  if (scale.value < 3) {
    scale.value += 0.25
    renderPage(currentPage.value)
  }
}

// 缩小
const zoomOut = () => {
  if (scale.value > 0.5) {
    scale.value -= 0.25
    renderPage(currentPage.value)
  }
}

// 监听 URL 变化
watch(() => props.url, () => {
  currentPage.value = 1
  loadPdf()
})

// 初始加载
onMounted(() => {
  loadPdf()
})

// 清理
onUnmounted(() => {
  if (pdfDoc.value) {
    pdfDoc.value.destroy()
  }
})
</script>

<style scoped>
.pdf-viewer {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.pdf-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-bottom: 1px solid #eee;
  background: #f5f7fa;
}

.zoom-level {
  min-width: 50px;
  text-align: center;
}

.page-info {
  min-width: 60px;
  text-align: center;
  color: #606266;
}

.pdf-loading {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.pdf-container {
  flex: 1;
  overflow: auto;
  display: flex;
  justify-content: center;
  padding: 20px;
  background: #525659;
}

.pdf-container canvas {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}
</style>