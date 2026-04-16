<template>
  <div class="trash-view">
    <div class="toolbar">
      <el-button type="danger" @click="handleClearAll">
        <el-icon><DeleteFilled /></el-icon>
        清空回收站
      </el-button>
    </div>
    <el-table :data="trashList" style="width: 100%">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="type" label="类型" width="80" />
      <el-table-column prop="deletedAt" label="删除时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleRestore(row)">恢复</el-button>
          <el-button link type="danger" @click="handleDelete(row)">彻底删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DeleteFilled } from '@element-plus/icons-vue'
import request from '../api/request'

const trashList = ref([])

const loadTrash = async () => {
  try {
    const data = await request.get('/trash')
    trashList.value = data
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleRestore = async (row) => {
  try {
    await request.post(`/trash/${row.id}/restore`)
    ElMessage.success('恢复成功')
    loadTrash()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('彻底删除后无法恢复，是否继续？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/trash/${row.id}`)
    ElMessage.success('删除成功')
    loadTrash()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message)
    }
  }
}

const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm('清空回收站后所有内容将无法恢复，是否继续？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete('/trash/clear')
    ElMessage.success('清空成功')
    loadTrash()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message)
    }
  }
}

onMounted(() => {
  loadTrash()
})
</script>

<style scoped>
.trash-view {
  padding: 20px;
}
.toolbar {
  margin-bottom: 20px;
}
</style>