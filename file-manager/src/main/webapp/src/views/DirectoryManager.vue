<template>
  <div class="directory-manager">
    <el-card class="tree-panel">
      <template #header>
        <div class="panel-header">
          <span>目录树</span>
          <el-button type="text" @click="loadTree">刷新</el-button>
        </div>
      </template>
      <el-tree
        :data="treeData"
        :props="treeProps"
        @node-click="handleNodeClick"
        @node-expand="handleNodeExpand"
        node-key="id"
        default-expand-all
      >
        <template #default="{ node, data }">
          <span class="tree-node">
            <span class="tree-icon">📁</span>
            <span>{{ data.name }}</span>
            <span class="tree-actions">
              <el-button type="text" size="small" @click.stop="createSubDirectory(data)">+新建</el-button>
            <el-button type="text" size="small" @click.stop="renameDirectory(data)">重命名</el-button>
              <el-button type="text" size="small" @click.stop="deleteDirectory(data)">删除</el-button>
            </span>
          </span>
        </template>
      </el-tree>
    </el-card>

    <el-card class="list-panel">
      <template #header>
        <div class="panel-header">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.id" @click="navigateTo(item.id)">
              {{ item.name }}
            </el-breadcrumb-item>
          </el-breadcrumb>
          <div class="toolbar">
            <el-button type="primary" @click="showCreateDialog">新建目录</el-button>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索目录..."
              style="width: 200px; margin-left: 10px;"
              @keyup.enter="handleSearch"
            >
              <template #append>
                <el-button :icon="Search" @click="handleSearch" />
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <el-table :data="directoryList" @row-click="handleRowClick">
        <el-table-column prop="name" label="目录名称">
          <template #default="{ row }">
            <span class="dir-name">
              <span class="dir-icon">📁</span>
              {{ row.name }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="text" @click.stop="renameDirectory(row)">重命名</el-button>
            <el-button type="text" @click.stop="moveDirectory(row)">移动</el-button>
            <el-button type="text" @click.stop="deleteDirectory(row)" style="color: #f56c6c;">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="directoryList.length === 0" description="暂无目录" />
    </el-card>

    <!-- 创建目录对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建目录" width="400px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="目录名称">
          <el-input v-model="createForm.name" placeholder="请输入目录名称" />
        </el-form-item>
        <el-form-item label="父目录">
          <el-input :model-value="currentParentName" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重命名对话框 -->
    <el-dialog v-model="renameDialogVisible" title="重命名目录" width="400px">
      <el-form :model="renameForm" label-width="80px">
        <el-form-item label="新名称">
          <el-input v-model="renameForm.name" placeholder="请输入新名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renameDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRename">确定</el-button>
      </template>
    </el-dialog>

    <!-- 确认删除对话框 -->
    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="400px">
      <p>确定要删除目录 "{{ deleteForm.name }}" 吗？</p>
      <p style="color: #e6a23c;">删除后，目录将移入回收站，30天后自动清除</p>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete">确认删除</el-button>
      </template>
    </el-dialog>

    <!-- 移动目录对话框 -->
    <el-dialog v-model="moveDialogVisible" title="移动目录" width="400px">
      <el-tree
        :data="treeData"
        :props="treeProps"
        :filter-node-method="filterMoveTree"
        @node-click="handleMoveSelect"
        node-key="id"
        ref="moveTreeRef"
      >
        <template #default="{ data }">
          <span>📁 {{ data.name }}</span>
        </template>
      </el-tree>
      <template #footer>
        <el-button @click="moveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmMove">确定移动</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as dirApi from '../api/directory'

const userId = ref(1)
const treeData = ref([])
const directoryList = ref([])
const currentParentId = ref(null)
const currentParentName = ref('根目录')
const breadcrumbs = ref([{ id: null, name: '根目录' }])
const searchKeyword = ref('')

const treeProps = { children: 'children', label: 'name' }

// 对话框状态
const createDialogVisible = ref(false)
const createForm = ref({ name: '', parentId: null })
const renameDialogVisible = ref(false)
const renameForm = ref({ id: null, name: '' })
const deleteDialogVisible = ref(false)
const deleteForm = ref({ id: null, name: '' })
const moveDialogVisible = ref(false)
const moveForm = ref({ id: null, parentId: null })

const moveTreeRef = ref(null)

// 加载目录树
const loadTree = async () => {
  try {
    const res = await dirApi.getDirectoryTree(userId.value)
    if (res.code === 0) {
      // 转换树结构
      treeData.value = buildTree(res.data)
    }
  } catch (e) {
    ElMessage.error('加载目录树失败')
  }
}

// 转换平铺数据为树形
const buildTree = (list) => {
  const map = {}
  const roots = []
  list.forEach(item => {
    item.children = []
    map[item.id] = item
  })
  list.forEach(item => {
    if (item.parentId === null || item.parentId === 0) {
      roots.push(item)
    } else {
      const parent = map[item.parentId]
      if (parent) {
        parent.children = parent.children || []
        parent.children.push(item)
      }
    }
  })
  return roots
}

// 加载当前目录的子目录
const loadChildDirectories = async (parentId) => {
  try {
    const res = await dirApi.getChildDirectories(parentId)
    if (res.code === 0) {
      directoryList.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('加载目录失败')
  }
}

// 获取面包屑
const loadBreadcrumb = async (id) => {
  if (!id) {
    breadcrumbs.value = [{ id: null, name: '根目录' }]
    return
  }
  try {
    const res = await dirApi.getBreadcrumb(id)
    if (res.code === 0) {
      const list = res.data || []
      breadcrumbs.value = [{ id: null, name: '根目录' }, ...list]
    }
  } catch (e) {
    console.error('加载面包屑失败', e)
  }
}

// 点击目录树节点
const handleNodeClick = async (data) => {
  currentParentId.value = data.id
  currentParentName.value = data.name
  await loadChildDirectories(data.id)
  await loadBreadcrumb(data.id)
}

// 展开目录树节点
const handleNodeExpand = async (data) => {
  // 懒加载子节点
}

// 点击列表行
const handleRowClick = async (row) => {
  currentParentId.value = row.id
  currentParentName.value = row.name
  await loadChildDirectories(row.id)
  await loadBreadcrumb(row.id)
}

// 导航到指定目录
const navigateTo = async (id) => {
  if (id === null) {
    currentParentId.value = null
    currentParentName.value = '根目录'
    await loadRootDirectories()
    await loadBreadcrumb(null)
  } else {
    const node = findNodeById(treeData.value, id)
    if (node) {
      currentParentId.value = id
      currentParentName.value = node.name
      await loadChildDirectories(id)
      await loadBreadcrumb(id)
    }
  }
}

const findNodeById = (nodes, id) => {
  for (const node of nodes) {
    if (node.id === id) return node
    if (node.children) {
      const found = findNodeById(node.children, id)
      if (found) return found
    }
  }
  return null
}

// 加载根目录
const loadRootDirectories = async () => {
  try {
    const res = await dirApi.getRootDirectories(userId.value)
    if (res.code === 0) {
      directoryList.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('加载目录失败')
  }
}

// 创建子目录
const createSubDirectory = (parentData) => {
  createForm.value = { name: '', parentId: parentData.id }
  currentParentName.value = parentData.name
  createDialogVisible.value = true
}

// 显示创建对话框
const showCreateDialog = () => {
  createForm.value = { name: '', parentId: currentParentId.value }
  createDialogVisible.value = true
}

// 确认创建
const confirmCreate = async () => {
  if (!createForm.value.name) {
    ElMessage.warning('请输入目录名称')
    return
  }
  try {
    const res = await dirApi.createDirectory({
      name: createForm.value.name,
      parentId: createForm.value.parentId || 0,
      userId: userId.value
    })
    if (res.code === 0) {
      ElMessage.success('创建成功')
      createDialogVisible.value = false
      await loadTree()
      await loadChildDirectories(currentParentId.value)
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (e) {
    ElMessage.error('创建失败')
  }
}

// 重命名
const renameDirectory = (row) => {
  renameForm.value = { id: row.id, name: '' }
  renameDialogVisible.value = true
}

// 确认重命名
const confirmRename = async () => {
  if (!renameForm.value.name) {
    ElMessage.warning('请输入新名称')
    return
  }
  try {
    const res = await dirApi.updateDirectory({
      id: renameForm.value.id,
      name: renameForm.value.name
    })
    if (res.code === 0 || res.code === undefined) {
      ElMessage.success('重命名成功')
      renameDialogVisible.value = false
      await loadTree()
      await loadChildDirectories(currentParentId.value)
    } else {
      ElMessage.error(res.message || '重命名失败')
    }
  } catch (e) {
    ElMessage.error('重命名失败')
  }
}

// 删除
const deleteDirectory = (row) => {
  deleteForm.value = { id: row.id, name: row.name }
  deleteDialogVisible.value = true
}

// 确认删除
const confirmDelete = async () => {
  try {
    const res = await dirApi.deleteDirectory(deleteForm.value.id)
    if (res.code === 0 || res.code === undefined) {
      ElMessage.success('已移入回收站')
      deleteDialogVisible.value = false
      await loadTree()
      await loadChildDirectories(currentParentId.value)
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

// 移动
const moveDirectory = (row) => {
  moveForm.value = { id: row.id, parentId: null }
  moveDialogVisible.value = true
}

// 移动树选择
const handleMoveSelect = (data) => {
  moveForm.value.parentId = data.id
}

// 过滤移动目标（排除自身和子目录）
const filterMoveTree = (value, data) => {
  return data.id !== moveForm.value.id
}

// 确认移动
const confirmMove = async () => {
  if (!moveForm.value.parentId) {
    ElMessage.warning('请选择目标目录')
    return
  }
  try {
    const res = await dirApi.updateDirectory({
      id: moveForm.value.id,
      parentId: moveForm.value.parentId
    })
    if (res.code === 0 || res.code === undefined) {
      ElMessage.success('移动成功')
      moveDialogVisible.value = false
      await loadTree()
      await loadChildDirectories(currentParentId.value)
    } else {
      ElMessage.error(res.message || '移动失败')
    }
  } catch (e) {
    ElMessage.error('移动失败')
  }
}

// 搜索
const handleSearch = async () => {
  if (!searchKeyword.value) {
    await loadChildDirectories(currentParentId.value)
    return
  }
  try {
    const res = await dirApi.searchDirectories(searchKeyword.value, userId.value)
    if (res.code === 0) {
      directoryList.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('搜索失败')
  }
}

onMounted(async () => {
  await loadTree()
  await loadRootDirectories()
})
</script>

<style scoped>
.directory-manager {
  display: flex;
  gap: 20px;
  height: 100%;
}

.tree-panel {
  width: 280px;
  flex-shrink: 0;
}

.list-panel {
  flex: 1;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar {
  display: flex;
  align-items: center;
  margin-top: 10px;
}

.tree-node {
  display: flex;
  align-items: center;
  width: 100%;
}

.tree-icon {
  margin-right: 5px;
}

.tree-actions {
  margin-left: auto;
  display: none;
}

.tree-node:hover .tree-actions {
  display: inline;
}

.dir-name {
  display: flex;
  align-items: center;
}

.dir-icon {
  margin-right: 5px;
}
</style>