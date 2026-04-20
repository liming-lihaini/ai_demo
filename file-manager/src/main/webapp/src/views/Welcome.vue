<template>
  <div class="welcome">
    <el-empty description="欢迎使用文件管理系统">
      <el-button type="primary" @click="$router.push('/files')">进入文件管理</el-button>
    </el-empty>
  </div>
</template>

<style scoped>
.welcome {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}
</style>