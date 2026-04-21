<template>
  <div class="home">
    <el-container>
      <el-header>
        <div class="header-left">
          <h1>文件管理系统</h1>
        </div>
        <div class="header-center">
          <el-input
            v-model="globalSearchKeyword"
            placeholder="搜索文件或目录..."
            class="global-search"
            @keyup.enter="handleGlobalSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="header-right">
          <el-button :icon="isCollapse ? Expand : Fold" text @click="handleCollapse" />
        </div>
      </el-header>
      <el-container>
        <el-aside :width="isCollapse ? '64px' : '200px'">
          <el-menu
            :default-active="activeMenu"
            :collapse="isCollapse"
            router
            collapse-transition
          >
            <el-menu-item index="/">
              <el-icon><HomeFilled /></el-icon>
              <template #title>首页</template>
            </el-menu-item>
            <el-menu-item index="/files">
              <el-icon><Folder /></el-icon>
              <template #title>文件管理</template>
            </el-menu-item>
            <el-menu-item index="/search">
              <el-icon><Search /></el-icon>
              <template #title>全文搜索</template>
            </el-menu-item>
            <el-menu-item index="/trash">
              <el-icon><Delete /></el-icon>
              <template #title>回收站</template>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main>
          <router-view v-slot="{ Component }">
            <transition name="el-fade-in-linear" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeFilled, Folder, Delete, Expand, Fold, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const activeMenu = computed(() => route.path)
const isCollapse = ref(false)
const globalSearchKeyword = ref('')

const handleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleGlobalSearch = () => {
  if (!globalSearchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  router.push({ path: '/search', query: { q: globalSearchKeyword.value } })
}
</script>

<style scoped>
.home {
  height: 100vh;
}

.el-header {
  background-color: #409eff;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
}

.header-left h1 {
  margin: 0;
  font-size: 18px;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 0 20px;
}

.global-search {
  width: 400px;
}

.global-search :deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.9);
}

.el-aside {
  background-color: #fff;
  transition: width 0.3s;
  overflow: hidden;
}

.el-aside :deep(.el-menu) {
  border-right: none;
}

.el-main {
  padding: 0;
  background-color: #f5f7fa;
  overflow: auto;
}
</style>