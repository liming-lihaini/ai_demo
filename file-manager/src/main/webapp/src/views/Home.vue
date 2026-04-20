<template>
  <div class="home">
    <el-container>
      <el-header>
        <div class="header-left">
          <h1>文件管理系统</h1>
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
import { useRoute } from 'vue-router'
import { HomeFilled, Folder, Delete, Expand, Fold } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
const isCollapse = ref(false)

const handleCollapse = () => {
  isCollapse.value = !isCollapse.value
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