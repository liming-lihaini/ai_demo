<template>
  <div class="file-manager-container">
    <!-- 左侧目录管理面板 -->
    <div class="directory-panel">
      <div class="panel-header">
        <h3>目录管理</h3>
        <el-button type="primary" size="small" :icon="FolderAdd" @click="showCreateDirDialog">新建</el-button>
      </div>

      <!-- 目录树 -->
      <el-tree
        ref="dirTreeRef"
        :data="directoryTree"
        :props="treeProps"
        node-key="id"
        highlight-current
        :expand-on-click-node="false"
        @node-click="handleNodeClick"
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <span class="node-label">
              <el-icon><Folder /></el-icon>
              {{ data.name }}
            </span>
            <div class="node-actions" @click.stop>
              <el-dropdown trigger="click">
                <el-button :icon="MoreFilled" size="small" text />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleCreateSubDir(data)">新建子目录</el-dropdown-item>
                    <el-dropdown-item @click="handleRenameDir(data)">重命名</el-dropdown-item>
                    <el-dropdown-item @click="handleMoveDir(data)">移动</el-dropdown-item>
                    <el-dropdown-item @click="handleDeleteDir(data)" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </template>
      </el-tree>
    </div>

    <!-- 右侧文件管理面板 -->
    <div class="file-panel">
      <!-- 面包屑 -->
      <div class="breadcrumb">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.id">
            <a @click="navigateToDir(item.id)">{{ item.name }}</a>
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 工具栏 -->
      <div class="toolbar">
        <el-button type="primary" :icon="Upload" @click="triggerUpload">上传文件</el-button>
        <el-button :icon="DocumentAdd" @click="handleCreateMd">新建MD文件</el-button>
        <el-button :icon="FolderAdd" @icon="FolderAdd" @click="handleCreateDirInPanel">新建目录</el-button>

        <div class="toolbar-right">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索文件..."
            style="width: 180px;"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </div>
      </div>

      <!-- 隐藏的上传组件 -->
      <el-upload
        ref="uploadRef"
        class="hidden-upload"
        :action="uploadUrl"
        :data="uploadParams"
        :show-file-list="false"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
        multiple
      />

      <!-- 文件列表 -->
      <div class="file-list" v-if="fileList.length > 0">
        <div
          v-for="file in fileList"
          :key="file.id"
          class="file-item"
          @click="handleFileClick(file)"
          @dblclick="handleFileDblClick(file)"
        >
          <div class="file-icon">{{ getFileIcon(file.format) }}</div>
          <div class="file-info">
            <div class="file-name">{{ file.name }}</div>
            <div class="file-meta">
              <span>{{ formatSize(file.size) }}</span>
              <span>{{ file.createdAt }}</span>
            </div>
          </div>
          <div class="file-actions" @click.stop>
            <el-dropdown trigger="click">
              <el-button :icon="MoreFilled" text />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handlePreview(file)">预览</el-dropdown-item>
                  <el-dropdown-item @click="handleDownload(file)">下载</el-dropdown-item>
                  <el-dropdown-item @click="handleEditMd(file)" v-if="file.format === 'md'">编辑</el-dropdown-item>
                  <el-dropdown-item @click="handleMoveFile(file)" divided>移动</el-dropdown-item>
                  <el-dropdown-item @click="handleDeleteFile(file)">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-else description="该目录下暂无文件" />
    </div>

    <!-- 新建目录对话框 -->
    <el-dialog v-model="createDirVisible" title="新建目录" width="400px">
      <el-form @submit.prevent="confirmCreateDir">
        <el-form-item label="目录名称">
          <el-input v-model="newDirName" placeholder="请输入目录名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDirVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCreateDir">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重命名目录对话框 -->
    <el-dialog v-model="renameDirVisible" title="重命名目录" width="400px">
      <el-form @submit.prevent="confirmRenameDir">
        <el-form-item label="目录名称">
          <el-input v-model="renameDirName" placeholder="请输入新名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renameDirVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRenameDir">确定</el-button>
      </template>
    </el-dialog>

    <!-- 移动目录对话框 -->
    <el-dialog v-model="moveDirVisible" title="移动目录" width="400px">
      <el-tree
        :data="directoryTree"
        :props="treeProps"
        node-key="id"
        highlight-current
        :expand-on-click-node="false"
        @node-click="handleMoveDirSelect"
      >
        <template #default="{ data }">
          <span>
            <el-icon><Folder /></el-icon>
            {{ data.name }}
          </span>
        </template>
      </el-tree>
      <template #footer>
        <el-button @click="moveDirVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmMoveDir">确定</el-button>
      </template>
    </el-dialog>

    <!-- 删除目录确认对话框 -->
    <el-dialog v-model="deleteDirVisible" title="确认删除" width="400px">
      <p>确定要删除目录 <strong>{{ deleteDirName }}</strong> 吗？</p>
      <p style="color: #e6a23c; margin-top: 10px;">删除后目录下的文件将移入回收站</p>
      <template #footer>
        <el-button @click="deleteDirVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDeleteDir">确认删除</el-button>
      </template>
    </el-dialog>

    <!-- 移动文件对话框 -->
    <el-dialog v-model="moveFileVisible" title="移动文件" width="400px">
      <el-tree
        :data="directoryTree"
        :props="treeProps"
        node-key="id"
        highlight-current
        :expand-on-click-node="false"
        @node-click="handleMoveFileSelect"
      >
        <template #default="{ data }">
          <span>
            <el-icon><Folder /></el-icon>
            {{ data.name }}
          </span>
        </template>
      </el-tree>
      <template #footer>
        <el-button @click="moveFileVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmMoveFile">确定</el-button>
      </template>
    </el-dialog>

    <!-- 删除文件确认对话框 -->
    <el-dialog v-model="deleteFileVisible" title="确认删除" width="400px">
      <p>确定要删除文件 <strong>{{ deleteFileName }}</strong> 吗？</p>
      <p style="color: #e6a23c; margin-top: 10px;">删除后文件将移入回收站</p>
      <template #footer>
        <el-button @click="deleteFileVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDeleteFile">确认删除</el-button>
      </template>
    </el-dialog>

    <!-- 新建MD文件对话框 -->
    <el-dialog v-model="createMdVisible" title="新建MD文件" width="400px">
      <el-form @submit.prevent="confirmCreateMd">
        <el-form-item label="文件名">
          <el-input v-model="newMdName" placeholder="请输入文件名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createMdVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCreateMd">确定</el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <PreviewDialog
      v-if="previewVisible"
      v-model:visible="previewVisible"
      :file-id="previewFileId"
      :file-name="previewFileName"
      :file-type="previewFileType"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Folder, FolderAdd, DocumentAdd, Upload, Search, MoreFilled } from '@element-plus/icons-vue'
import * as dirApi from '../api/directory'
import * as fileApi from '../api/file'
import PreviewDialog from '../components/PreviewDialog.vue'

const router = useRouter()
const userId = ref(1)

// 目录相关
const directoryTree = ref([])
const dirTreeRef = ref(null)
const currentDirId = ref(null)
const breadcrumbs = ref([])
const treeProps = { label: 'name', children: 'children' }

// 文件相关
const fileList = ref([])
const searchKeyword = ref('')
const uploadRef = ref(null)

// 对话框状态
const createDirVisible = ref(false)
const newDirName = ref('')
const createDirParentId = ref(null)

const renameDirVisible = ref(false)
const renameDirId = ref(null)
const renameDirName = ref('')

const moveDirVisible = ref(false)
const moveDirId = ref(null)
const moveDirTargetId = ref(null)

const deleteDirVisible = ref(false)
const deleteDirId = ref(null)
const deleteDirName = ref('')

const moveFileVisible = ref(false)
const moveFileId = ref(null)
const moveFileTargetId = ref(null)

const deleteFileVisible = ref(false)
const deleteFileId = ref(null)
const deleteFileName = ref('')

const createMdVisible = ref(false)
const newMdName = ref('')

// 预览相关
const previewVisible = ref(false)
const previewFileId = ref(null)
const previewFileName = ref('')
const previewFileType = ref('')

const uploadUrl = computed(() => `${window.location.origin}/api/file/upload`)
const uploadParams = computed(() => ({ userId: userId.value, parentId: currentDirId.value || 0 }))

// 加载目录树
const loadDirectoryTree = async () => {
  try {
    const res = await dirApi.getDirectoryTree(userId.value)
    if (res.code === 0) {
      directoryTree.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('加载目录失败')
  }
}

// 加载文件列表
const loadFileList = async () => {
  try {
    const res = await fileApi.getFileList(currentDirId.value || 0, userId.value)
    if (res.code === 0) {
      fileList.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('加载文件列表失败')
  }
}

// 加载面包屑
const loadBreadcrumb = async () => {
  if (!currentDirId.value) {
    breadcrumbs.value = [{ id: null, name: '根目录' }]
    return
  }
  try {
    const res = await dirApi.getBreadcrumb(currentDirId.value)
    if (res.code === 0) {
      breadcrumbs.value = [{ id: null, name: '根目录' }, ...(res.data || [])]
    }
  } catch (e) {
    breadcrumbs.value = [{ id: null, name: '根目录' }]
  }
}

// 点击目录节点
const handleNodeClick = (data) => {
  currentDirId.value = data.id
  loadBreadcrumb()
  loadFileList()
}

// 导航到指定目录
const navigateToDir = (dirId) => {
  currentDirId.value = dirId
  loadBreadcrumb()
  loadFileList()
}

// 显示新建目录对话框
const showCreateDirDialog = () => {
  createDirParentId.value = null
  newDirName.value = ''
  createDirVisible.value = true
}

// 在面板中新建目录
const handleCreateDirInPanel = () => {
  createDirParentId.value = currentDirId.value
  newDirName.value = ''
  createDirVisible.value = true
}

// 创建子目录
const handleCreateSubDir = (data) => {
  createDirParentId.value = data.id
  newDirName.value = ''
  createDirVisible.value = true
}

// 确认新建目录
const confirmCreateDir = async () => {
  if (!newDirName.value) {
    ElMessage.warning('请输入目录名称')
    return
  }
  try {
    const res = await dirApi.createDirectory(newDirName.value, createDirParentId.value, userId.value)
    if (res.code === 0) {
      ElMessage.success('创建成功')
      createDirVisible.value = false
      loadDirectoryTree()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (e) {
    ElMessage.error('创建失败')
  }
}

// 重命名目录
const handleRenameDir = (data) => {
  renameDirId.value = data.id
  renameDirName.value = data.name
  renameDirVisible.value = true
}

const confirmRenameDir = async () => {
  if (!renameDirName.value) {
    ElMessage.warning('请输入目录名称')
    return
  }
  try {
    const res = await dirApi.renameDirectory(renameDirId.value, renameDirName.value)
    if (res.code === 0) {
      ElMessage.success('重命名成功')
      renameDirVisible.value = false
      loadDirectoryTree()
      loadBreadcrumb()
    } else {
      ElMessage.error(res.message || '重命名失败')
    }
  } catch (e) {
    ElMessage.error('重命名失败')
  }
}

// 移动目录
const handleMoveDir = (data) => {
  moveDirId.value = data.id
  moveDirTargetId.value = null
  moveDirVisible.value = true
}

const handleMoveDirSelect = (data) => {
  moveDirTargetId.value = data.id
}

const confirmMoveDir = async () => {
  if (!moveDirTargetId.value) {
    ElMessage.warning('请选择目标目录')
    return
  }
  if (moveDirTargetId.value === moveDirId.value) {
    ElMessage.warning('不能移动到自身')
    return
  }
  try {
    const res = await dirApi.moveDirectory(moveDirId.value, moveDirTargetId.value)
    if (res.code === 0) {
      ElMessage.success('移动成功')
      moveDirVisible.value = false
      loadDirectoryTree()
    } else {
      ElMessage.error(res.message || '移动失败')
    }
  } catch (e) {
    ElMessage.error('移动失败')
  }
}

// 删除目录
const handleDeleteDir = (data) => {
  deleteDirId.value = data.id
  deleteDirName.value = data.name
  deleteDirVisible.value = true
}

const confirmDeleteDir = async () => {
  try {
    const res = await dirApi.deleteDirectory(deleteDirId.value)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      deleteDirVisible.value = false
      if (currentDirId.value === deleteDirId.value) {
        currentDirId.value = null
      }
      loadDirectoryTree()
      loadFileList()
      loadBreadcrumb()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

// 触发上传
const triggerUpload = () => {
  uploadRef.value?.$el.querySelector('input').click()
}

const beforeUpload = (file) => {
  const maxSize = 100 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过100MB')
    return false
  }
  return true
}

const handleUploadSuccess = () => {
  ElMessage.success('上传成功')
  loadFileList()
}

const handleUploadError = () => {
  ElMessage.error('上传失败')
}

// 搜索
const handleSearch = async () => {
  if (!searchKeyword.value) {
    loadFileList()
    return
  }
  try {
    const res = await fileApi.searchFiles(searchKeyword.value, userId.value)
    if (res.code === 0) {
      fileList.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('搜索失败')
  }
}

// 点击文件
const handleFileClick = (file) => {
  // 单击选中
}

// 双击文件
const handleFileDblClick = (file) => {
  if (file.format === 'md') {
    router.push({ path: '/editor', query: { id: file.id } })
  } else {
    handlePreview(file)
  }
}

// 预览
const handlePreview = (file) => {
  const type = getFileType(file.format)
  if (!type) {
    ElMessage.warning('该文件类型不支持预览')
    return
  }
  previewFileId.value = file.id
  previewFileName.value = file.name
  previewFileType.value = type
  previewVisible.value = true
}

// 编辑MD文件
const handleEditMd = (file) => {
  router.push({ path: '/editor', query: { id: file.id } })
}

// 下载
const handleDownload = async (file) => {
  try {
    const url = fileApi.getDownloadUrl(file.id)
    const link = document.createElement('a')
    link.href = url
    link.download = file.name
    link.click()
    ElMessage.success('开始下载')
  } catch (e) {
    ElMessage.error('下载失败')
  }
}

// 移动文件
const handleMoveFile = (file) => {
  moveFileId.value = file.id
  moveFileTargetId.value = null
  moveFileVisible.value = true
}

const handleMoveFileSelect = (data) => {
  moveFileTargetId.value = data.id
}

const confirmMoveFile = async () => {
  if (!moveFileTargetId.value) {
    ElMessage.warning('请选择目标目录')
    return
  }
  try {
    const res = await fileApi.moveFile(moveFileId.value, moveFileTargetId.value)
    if (res.code === 0) {
      ElMessage.success('移动成功')
      moveFileVisible.value = false
      loadFileList()
    } else {
      ElMessage.error(res.message || '移动失败')
    }
  } catch (e) {
    ElMessage.error('移动失败')
  }
}

// 删除文件
const handleDeleteFile = (file) => {
  deleteFileId.value = file.id
  deleteFileName.value = file.name
  deleteFileVisible.value = true
}

const confirmDeleteFile = async () => {
  try {
    const res = await fileApi.deleteFile(deleteFileId.value)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      deleteFileVisible.value = false
      loadFileList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

// 新建MD文件
const handleCreateMd = () => {
  newMdName.value = ''
  createMdVisible.value = true
}

const confirmCreateMd = async () => {
  if (!newMdName.value) {
    ElMessage.warning('请输入文件名')
    return
  }
  const name = newMdName.value.endsWith('.md') ? newMdName.value : newMdName.value + '.md'

  // 创建空的MD文件
  const content = '# ' + newMdName.value.replace('.md', '') + '\n\n'
  try {
    // 通过更新内容来创建文件
    // 这里需要先创建一个空文件记录，然后编辑
    ElMessage.info('请在编辑器中编辑内容后保存')
    createMdVisible.value = false

    // 跳转到编辑器创建新文件
    router.push({ path: '/editor', query: { parentId: currentDirId.value || 0, name: name } })
  } catch (e) {
    ElMessage.error('创建失败')
  }
}

// 获取文件图标
const getFileIcon = (format) => {
  if (!format) return '📄'
  const ext = format.toLowerCase()
  const icons = {
    pdf: '📕', doc: '📘', docx: '📘',
    xls: '📗', xlsx: '📗',
    txt: '📄', md: '📝',
    jpg: '🖼️', jpeg: '🖼️', png: '🖼️', gif: '🖼️', bmp: '🖼️',
    mp3: '🎵', wav: '🎵',
    mp4: '🎬', avi: '🎬'
  }
  return icons[ext] || '📄'
}

// 获取文件类型
const getFileType = (format) => {
  if (!format) return ''
  const ext = format.toLowerCase()
  if (['pdf'].includes(ext)) return 'pdf'
  if (['txt', 'md'].includes(ext)) return 'text'
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp'].includes(ext)) return 'image'
  if (['mp3', 'wav'].includes(ext)) return 'audio'
  if (['mp4', 'avi'].includes(ext)) return 'video'
  if (['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'].includes(ext)) return 'office'
  return ''
}

// 格式化文件大小
const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return `${size.toFixed(1)} ${units[unitIndex]}`
}

onMounted(() => {
  loadDirectoryTree()
  loadBreadcrumb()
  loadFileList()
})
</script>

<style scoped>
.file-manager-container {
  display: flex;
  height: calc(100vh - 60px);
  background: #f5f7fa;
}

.directory-panel {
  width: 280px;
  min-width: 280px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.directory-panel :deep(.el-tree) {
  flex: 1;
  overflow: auto;
  padding: 8px;
}

.tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 8px;
}

.node-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.node-actions {
  opacity: 0;
  transition: opacity 0.2s;
}

.tree-node:hover .node-actions {
  opacity: 1;
}

.file-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
  overflow: hidden;
}

.breadcrumb {
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 4px;
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}

.toolbar-right {
  margin-left: auto;
}

.hidden-upload {
  display: none;
}

.file-list {
  flex: 1;
  overflow: auto;
  background: #fff;
  border-radius: 4px;
  padding: 12px;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.file-item:hover {
  background: #f5f7fa;
}

.file-item:last-child {
  border-bottom: none;
}

.file-icon {
  font-size: 32px;
  margin-right: 12px;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-meta {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  display: flex;
  gap: 12px;
}

.file-actions {
  opacity: 0;
  transition: opacity 0.2s;
}

.file-item:hover .file-actions {
  opacity: 1;
}
</style>