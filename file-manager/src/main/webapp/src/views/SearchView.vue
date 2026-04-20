<template>
  <div class="search-view">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索文件或目录..."
        @keyup.enter="handleSearch"
        @input="handleInput"
        clearable
        class="search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>

      <!-- 筛选面板 -->
      <el-collapse v-model="activeFilters">
        <el-collapse-item title="筛选条件" name="filters">
          <div class="filter-panel">
            <el-form label-width="80px">
              <el-form-item label="文件类型">
                <el-select v-model="filters.fileType" placeholder="全部" clearable>
                  <el-option label="文档" value="doc,docx,xls,xlsx,ppt,pptx,pdf,txt,md" />
                  <el-option label="图片" value="jpg,jpeg,png,gif,bmp" />
                  <el-option label="音频" value="mp3,wav" />
                  <el-option label="视频" value="mp4,avi" />
                </el-select>
              </el-form-item>
              <el-form-item label="日期范围">
                <el-date-picker
                  v-model="filters.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                />
              </el-form-item>
              <el-form-item label="大小范围">
                <el-input-number v-model="filters.minSize" :min="0" placeholder="最小(KB)" />
                <span> - </span>
                <el-input-number v-model="filters.maxSize" :min="0" placeholder="最大(KB)" />
              </el-form-item>
            </el-form>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- 搜索结果 -->
    <div class="search-results">
      <div class="results-header">
        <span>搜索结果 ({{ total }})</span>
      </div>

      <el-empty v-if="!loading && total === 0 && searchKeyword" description="未找到匹配的文件或目录，请尝试其他关键词" />

      <!-- 文件结果 -->
      <div v-if="resultFiles.length > 0" class="result-section">
        <div class="section-title">文件</div>
        <el-table :data="resultFiles" style="width: 100%" @row-click="handleFileClick">
          <el-table-column prop="name" label="名称">
            <template #default="{ row }">
              <span v-html="highlightKeyword(row.name)"></span>
            </template>
          </el-table-column>
          <el-table-column prop="format" label="类型" width="80" />
          <el-table-column prop="size" label="大小" width="100">
            <template #default="{ row }">
              {{ formatSize(row.size) }}
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" />
        </el-table>
      </div>

      <!-- 目录结果 -->
      <div v-if="resultDirectories.length > 0" class="result-section">
        <div class="section-title">目录</div>
        <el-table :data="resultDirectories" style="width: 100%" @row-click="handleDirClick">
          <el-table-column prop="name" label="名称">
            <template #default="{ row }">
              <span v-html="highlightKeyword(row.name)"></span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" />
        </el-table>
      </div>
    </div>

    <!-- 搜索历史 -->
    <div class="search-history" v-if="searchHistory.length > 0 && !searchKeyword">
      <div class="history-title">搜索历史</div>
      <div class="history-tags">
        <el-tag
          v-for="keyword in searchHistory"
          :key="keyword"
          @click="handleHistoryClick(keyword)"
          class="history-tag"
        >
          {{ keyword }}
        </el-tag>
      </div>
      <el-button link type="primary" @click="handleClearHistory">清空历史</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { search, getSearchHistory, saveSearchHistory, clearSearchHistory } from '../api/search'

const router = useRouter()

const searchKeyword = ref('')
const loading = ref(false)
const resultFiles = ref([])
const resultDirectories = ref([])
const total = ref(0)
const searchHistory = ref([])
const activeFilters = ref([])
const timer = ref(null)

// 筛选条件
const filters = ref({
  fileType: '',
  dateRange: null,
  minSize: null,
  maxSize: null
})

// 加载搜索历史
onMounted(() => {
  searchHistory.value = getSearchHistory()
})

// 输入防抖
const handleInput = () => {
  if (timer.value) clearTimeout(timer.value)
  timer.value = setTimeout(() => {
    if (searchKeyword.value.trim()) {
      handleSearch()
    }
  }, 300)
}

// 执行搜索
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  loading.value = true
  try {
    const params = {
      q: searchKeyword.value,
      type: 'all',
      userId: 1,
      page: 1,
      size: 100
    }

    if (filters.value.fileType) {
      params.fileType = filters.value.fileType
    }

    if (filters.value.dateRange) {
      params.startDate = filters.value.dateRange[0]?.getTime()
      params.endDate = filters.value.dateRange[1]?.getTime()
    }

    if (filters.value.minSize) {
      params.minSize = filters.value.minSize * 1024
    }

    if (filters.value.maxSize) {
      params.maxSize = filters.value.maxSize * 1024
    }

    const res = await search(params)
    if (res.code === 200) {
      resultFiles.value = res.data.files || []
      resultDirectories.value = res.data.directories || []
      total.value = res.data.total || 0

      // 保存搜索历史
      saveSearchHistory(searchKeyword.value)
      searchHistory.value = getSearchHistory()
    }
  } catch (error) {
    ElMessage.error('搜索失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 点击搜索历史
const handleHistoryClick = (keyword) => {
  searchKeyword.value = keyword
  handleSearch()
}

// 清空历史
const handleClearHistory = () => {
  clearSearchHistory()
  searchHistory.value = []
}

// 关键词高亮
const highlightKeyword = (text) => {
  if (!searchKeyword.value || !text) return text
  const regex = new RegExp(`(${searchKeyword.value})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

// 格式化文件大小
const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 点击文件行
const handleFileClick = (row) => {
  router.push({ path: '/files', query: { parentId: row.parentId } })
}

// 点击目录行
const handleDirClick = (row) => {
  router.push({ path: '/files', query: { parentId: row.id } })
}
</script>

<style scoped>
.search-view {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.search-input {
  max-width: 600px;
  margin-bottom: 10px;
}

.filter-panel {
  padding: 10px;
}

.search-results {
  min-height: 300px;
}

.results-header {
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: bold;
}

.result-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #606266;
}

.search-history {
  margin-top: 30px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.history-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 10px;
}

.history-tags {
  margin-bottom: 10px;
}

.history-tag {
  margin-right: 10px;
  cursor: pointer;
}

:deep(mark) {
  background-color: #fdf6ec;
  color: #e6a23c;
  padding: 0 2px;
}
</style>
