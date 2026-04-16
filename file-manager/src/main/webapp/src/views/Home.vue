<template>
  <div class="home">
    <el-container>
      <el-header>
        <h1>文件管理系统</h1>
      </el-header>
      <el-container>
        <el-aside width="200px">
          <el-menu :default-active="activeMenu" router>
            <el-menu-item index="/">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/files">
              <el-icon><Folder /></el-icon>
              <span>文件管理</span>
            </el-menu-item>
            <el-menu-item index="/trash">
              <el-icon><Delete /></el-icon>
              <span>回收站</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { HomeFilled, Folder, Delete } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.home {
  height: 100vh;
}
.el-header {
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
}
.el-header h1 {
  margin: 0;
  font-size: 18px;
}
.el-aside {
  background-color: #f5f5f5;
}
</style>