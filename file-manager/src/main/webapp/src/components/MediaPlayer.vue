<template>
  <div class="media-player" :class="type">
    <!-- 音频模式 -->
    <div v-if="type === 'audio'" class="audio-player">
      <div class="audio-cover">
        <div class="audio-icon">🎵</div>
      </div>
      <div class="audio-info">
        <div class="audio-progress">
          <el-slider
            v-model="currentTime"
            :max="duration"
            :show-tooltip="false"
            @change="handleSeek"
          />
          <span class="time-display">{{ formatTime(currentTime) }} / {{ formatTime(duration) }}</span>
        </div>
        <div class="audio-controls">
          <el-button :icon="Backward" circle size="small" @click="skip(-10)" />
          <el-button :icon="isPlaying ? VideoPause : VideoPlay" circle size="large" @click="togglePlay" />
          <el-button :icon="Forward" circle size="small" @click="skip(10)" />
        </div>
        <div class="volume-control">
          <el-icon><Volume /></el-icon>
          <el-slider v-model="volume" :min="0" :max="100" @change="handleVolumeChange" />
        </div>
      </div>
    </div>

    <!-- 视频模式 -->
    <div v-else class="video-player">
      <video
        ref="videoRef"
        :src="url"
        :poster="poster"
        @loadedmetadata="handleLoadedMetadata"
        @timeupdate="handleTimeUpdate"
        @play="isPlaying = true"
        @pause="isPlaying = false"
        @ended="isPlaying = false"
        @error="handleError"
      />

      <!-- -controls -->
      <div class="video-toolbar">
        <el-button :icon="isPlaying ? VideoPause : VideoPlay" circle size="small" @click="togglePlay" />
        <div class="progress-bar">
          <el-slider
            v-model="currentTime"
            :max="duration"
            :show-tooltip="false"
            @change="handleSeek"
          />
          <span class="time-display">{{ formatTime(currentTime) }} / {{ formatTime(duration) }}</span>
        </div>
        <el-button-group>
          <el-button :icon="Backward" size="small" @click="skip(-10)" />
          <el-button :icon="Forward" size="small" @click="skip(10)" />
        </el-button-group>
        <div class="volume-control">
          <el-icon><Volume /></el-icon>
          <el-slider v-model="volume" :min="0" :max="100" style="width: 80px;" @change="handleVolumeChange" />
        </div>
        <el-button :icon="FullScreen" size="small" @click="toggleFullscreen" />
      </div>

      <!-- Loading -->
      <div v-if="loading" class="video-loading">
        <el-progress :percentage="loadProgress" :status="loadStatus" />
        <p>正在加载媒体...</p>
      </div>
    </div>

    <!-- Error -->
    <div v-if="error" class="media-error">
      <el-result icon="error" :title="error">
        <template #sub-title>
          <el-button type="primary" @click="retry">重试</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { VideoPlay, VideoPause, Backward, Forward, Volume, FullScreen } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  url: {
    type: String,
    required: true
  },
  fileId: {
    type: Number,
    required: true
  },
  type: {
    type: String,
    default: 'video', // audio | video
    validator: (val) => ['audio', 'video'].includes(val)
  },
  poster: {
    type: String,
    default: ''
  }
})

// 状态
const loading = ref(true)
const loadProgress = ref(0)
const loadStatus = ref('')
const error = ref('')
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const volume = ref(80)

const videoRef = ref(null)

// 播放/暂停
const togglePlay = () => {
  if (!videoRef.value) return

  if (isPlaying.value) {
    videoRef.value.pause()
  } else {
    videoRef.value.play()
  }
}

// 跳转
const handleSeek = (value) => {
  if (!videoRef.value) return
  videoRef.value.currentTime = value
}

// 跳过
const skip = (seconds) => {
  if (!videoRef.value) return
  const newTime = videoRef.value.currentTime + seconds
  videoRef.value.currentTime = Math.max(0, Math.min(newTime, duration.value))
}

// 音量变化
const handleVolumeChange = (value) => {
  if (!videoRef.value) return
  videoRef.value.volume = value / 100
}

// 元数据加载
const handleLoadedMetadata = () => {
  loading.value = false
  if (videoRef.value) {
    duration.value = videoRef.value.duration || 0
    videoRef.value.volume = volume.value / 100
  }
}

// 时间更新
const handleTimeUpdate = () => {
  if (videoRef.value) {
    currentTime.value = videoRef.value.currentTime
  }
}

// 错误
const handleError = () => {
  loading.value = false
  error.value = '加载媒体失败'
  ElMessage.error('加载媒体失败')
}

// 重试
const retry = () => {
  error.value = ''
  loading.value = true
  if (videoRef.value) {
    videoRef.value.load()
  }
}

// 全屏
const toggleFullscreen = () => {
  if (!videoRef.value) return
  if (document.fullscreenElement) {
    document.exitFullscreen()
  } else {
    videoRef.value.requestFullscreen()
  }
}

// 格式化时间
const formatTime = (seconds) => {
  if (!seconds || isNaN(seconds)) return '00:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 监听 url 变化
watch(() => props.url, () => {
  loading.value = true
  error.value = ''
  currentTime.value = 0
  duration.value = 0
})

onMounted(() => {
  // 可选：自动播放
})
</script>

<style scoped>
.media-player {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 音频模式 */
.audio-player {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 12px;
}

.audio-cover {
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  margin-bottom: 20px;
}

.audio-icon {
  font-size: 60px;
}

.audio-info {
  width: 100%;
  max-width: 300px;
}

.audio-progress {
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin-bottom: 20px;
}

.time-display {
  font-size: 12px;
  text-align: center;
  opacity: 0.8;
}

.audio-controls {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-bottom: 20px;
}

.volume-control {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 视频模式 */
.video-player {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #000;
  position: relative;
}

.video-player video {
  flex: 1;
  width: 100%;
  object-fit: contain;
}

.video-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
}

.progress-bar {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
}

.time-display {
  min-width: 80px;
  font-size: 12px;
}

.volume-control {
  display: flex;
  align-items: center;
  gap: 5px;
}

.video-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.media-error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
</style>