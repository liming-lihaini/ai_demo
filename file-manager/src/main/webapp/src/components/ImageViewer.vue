<template>
  <div class="image-viewer">
    <!-- 工具栏 -->
    <div class="image-toolbar">
      <el-button-group>
        <el-button :icon="ZoomOut" size="small" @click="zoomOut">缩小</el-button>
        <el-button :icon="ZoomIn" size="small" @click="zoomIn">放大</el-button>
      </el-button-group>
      <span class="zoom-level">{{ Math.round(scale * 100) }}%</span>
      <el-button-group>
        <el-button size="small" @click="rotateLeft">左转</el-button>
        <el-button size="small" @click="rotateRight">右转</el-button>
      </el-button-group>
      <span v-if="images.length > 1" class="page-info">
        {{ currentIndex + 1 }} / {{ images.length }}
        <el-button size="small" :disabled="currentIndex <= 0" @click="prevImage">上一张</el-button>
        <el-button size="small" :disabled="currentIndex >= images.length - 1" @click="nextImage">下一张</el-button>
      </span>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="image-loading">
      <el-progress :percentage="loadProgress" :status="loadStatus" />
      <p>正在加载图片...</p>
    </div>

    <!-- 图片容器 -->
    <div v-else ref="imageContainer" class="image-container" @wheel="handleWheel">
      <img
        ref="imageRef"
        :src="currentImage"
        :style="imageStyle"
        @load="handleLoad"
        @error="handleError"
        @click="handleClick"
      />
    </div>

    <!-- 缩略图列表 -->
    <div v-if="images.length > 1" class="thumbnail-list">
      <div class="thumbnail-wrapper">
        <img
          v-for="(img, index) in images"
          :key="index"
          :src="img"
          :class="['thumbnail', { active: index === currentIndex }]"
          @click="currentIndex = index"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ZoomIn, ZoomOut } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  images: {
    type: Array,
    default: () => []
  },
  initialIndex: {
    type: Number,
    default: 0
  }
})

// 状态
const loading = ref(true)
const loadProgress = ref(0)
const loadStatus = ref('')
const currentIndex = ref(0)
const scale = ref(1)
const rotation = ref(0)
const imageRef = ref(null)
const imageContainer = ref(null)

// 当前图片
const currentImage = computed(() => {
  return props.images[currentIndex.value] || ''
})

// 图片样式
const imageStyle = computed(() => {
  return {
    transform: `scale(${scale.value}) rotate(${rotation.value}deg)`,
    transition: 'transform 0.3s ease'
  }
})

// 加载图片
const loadImage = () => {
  loading.value = true
}

// 图片加载完成
const handleLoad = () => {
  loading.value = false
  loadStatus.value = 'success'
}

// 图片加载失败
const handleError = () => {
  loading.value = false
  loadStatus.value = 'exception'
  ElMessage.error('加载图片失败')
}

// 点击（放大/缩小切换）
const handleClick = () => {
  if (scale.value < 2) {
    scale.value = 2
  } else {
    scale.value = 1
  }
}

// 滚轮缩放
const handleWheel = (e) => {
  e.preventDefault()
  if (e.deltaY < 0) {
    zoomIn()
  } else {
    zoomOut()
  }
}

// 放大
const zoomIn = () => {
  if (scale.value < 3) {
    scale.value += 0.25
  }
}

// 缩小
const zoomOut = () => {
  if (scale.value > 0.25) {
    scale.value -= 0.25
  }
}

// 左转
const rotateLeft = () => {
  rotation.value -= 90
}

// 右转
const rotateRight = () => {
  rotation.value += 90
}

// 上一张
const prevImage = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
    resetView()
  }
}

// 下一张
const nextImage = () => {
  if (currentIndex.value < props.images.length - 1) {
    currentIndex.value++
    resetView()
  }
}

// 重置视图
const resetView = () => {
  scale.value = 1
  rotation.value = 0
  loading.value = true
}

// 监听 initialIndex 变化
watch(() => props.initialIndex, (val) => {
  currentIndex.value = val
})
</script>

<style scoped>
.image-viewer {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.image-toolbar {
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
  display: flex;
  align-items: center;
  gap: 10px;
  color: #606266;
}

.image-loading {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.image-container {
  flex: 1;
  overflow: auto;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #000;
  cursor: pointer;
}

.image-container img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.thumbnail-list {
  padding: 10px;
  border-top: 1px solid #eee;
  background: #f5f7fa;
  overflow-x: auto;
}

.thumbnail-wrapper {
  display: flex;
  gap: 10px;
}

.thumbnail {
  width: 60px;
  height: 60px;
  object-fit: cover;
  cursor: pointer;
  border: 2px solid transparent;
  border-radius: 4px;
  transition: border-color 0.2s;
}

.thumbnail:hover {
  border-color: #409eff;
}

.thumbnail.active {
  border-color: #409eff;
}
</style>