<template>
  <div class="editor-view">
    <div class="toolbar">
      <el-button type="primary" @click="handleSave" :loading="saving">
        <el-icon><FolderChecked /></el-icon>
        保存
      </el-button>
      <el-button @click="handleExport('md')">导出MD</el-button>
      <el-button @click="handleExport('pdf')">导出PDF</el-button>
      <el-button @click="handleExport('html')">导出HTML</el-button>
      <el-button @click="goBack">
        <el-icon><Back /></el-icon>
        返回
      </el-button>
    </div>
    <div class="editor-container">
      <div class="editor-pane">
        <textarea id="editor"></textarea>
      </div>
      <div class="preview-pane" v-html="previewContent"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import EasyMDE from 'easymde'
import { ElMessage } from 'element-plus'
import { FolderChecked, Back } from '@element-plus/icons-vue'
import { updateFileContent } from '../api/fileContent'
import { getTextPreview } from '../api/preview'

const route = useRoute()
const router = useRouter()

const fileId = ref(route.query.id)
const saving = ref(false)
const previewContent = ref('')
let easyMDE = null
let currentContent = ''

const initEditor = async () => {
  // 加载文件内容
  if (!fileId.value) {
    ElMessage.error('缺少文件ID')
    return
  }

  try {
    const res = await getTextPreview(parseInt(fileId.value))
    if (res.code === 0) {
      currentContent = res.data || ''
    } else {
      currentContent = ''
    }
  } catch (e) {
    currentContent = ''
  }

  // 初始化 EasyMDE
  await nextTick()
  easyMDE = new EasyMDE({
    element: document.getElementById('editor'),
    initialValue: currentContent,
    spellChecker: false,
    autosave: {
      enabled: false
    },
    toolbar: ['bold', 'italic', 'heading', '|', 'code', 'quote', '|', 'unordered-list', 'ordered-list', '|', 'link', 'image', '|', 'preview', 'side-by-side', 'fullscreen'],
    status: false,
    placeholder: '在此输入 Markdown 内容...'
  })

  // 实时预览更新
  easyMDE.codemirror.on('change', () => {
    updatePreview()
  })

  updatePreview()
}

const updatePreview = () => {
  if (!easyMDE) return
  const rawContent = easyMDE.value()

  // 简单 Markdown 渲染
  previewContent.value = renderMarkdown(rawContent)
}

// 简单 Markdown 渲染器
const renderMarkdown = (content) => {
  if (!content) return ''

  let html = content
    // 转义 HTML
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    // 标题
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    // 粗体斜体
    .replace(/\*\*\*(.+?)\*\*\*/g, '<strong><em>$1</em></strong>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    // 代码块
    .replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
    // 行内代码
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    // 链接
    .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
    // 图片
    .replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1">')
    // 列表
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/^\d+\. (.+)$/gm, '<li>$1</li>')
    // 换行
    .replace(/\n/g, '<br>')

  return html
}

const handleSave = async () => {
  if (!fileId.value) {
    ElMessage.error('文件ID无效')
    return
  }

  saving.value = true
  try {
    const content = easyMDE ? easyMDE.value() : ''
    const res = await updateFileContent(parseInt(fileId.value), content)
    if (res.code === 0) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败: ' + e.message)
  } finally {
    saving.value = false
  }
}

const handleExport = (format) => {
  if (!easyMDE) return
  const content = easyMDE.value()

  switch (format) {
    case 'md':
      // 直接下载 MD 文件
      const blob = new Blob([content], { type: 'text/markdown' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = 'export.md'
      a.click()
      URL.revokeObjectURL(url)
      ElMessage.success('导出 MD 成功')
      break
    case 'html':
      // 导出 HTML
      const htmlContent = `<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Markdown Export</title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 800px; margin: 50px auto; padding: 20px; }
    h1, h2, h3 { color: #333; }
    code { background: #f5f5f5; padding: 2px 6px; border-radius: 3px; }
    pre { background: #f5f5f5; padding: 15px; border-radius: 5px; overflow-x: auto; }
    img { max-width: 100%; }
    a { color: #409eff; }
  </style>
</head>
<body>${renderMarkdown(content)}</body>
</html>`
      const htmlBlob = new Blob([htmlContent], { type: 'text/html' })
      const htmlUrl = URL.createObjectURL(htmlBlob)
      const htmlA = document.createElement('a')
      htmlA.href = htmlUrl
      htmlA.download = 'export.html'
      htmlA.click()
      URL.revokeObjectURL(htmlUrl)
      ElMessage.success('导出 HTML 成功')
      break
    case 'pdf':
      ElMessage.info('PDF 导出功能开发中')
      break
  }
}

const goBack = () => {
  router.push('/files')
}

onMounted(() => {
  initEditor()
})

onBeforeUnmount(() => {
  if (easyMDE) {
    easyMDE = null
  }
})
</script>

<style scoped>
.editor-view {
  height:100vh;
  display: flex;
  flex-direction: column;
}

.toolbar {
  padding: 10px 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  gap: 10px;
  background: #fff;
}

.toolbar .el-button {
  display: flex;
  align-items: center;
  gap: 4px;
}

.editor-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.editor-pane {
  flex: 1;
  overflow: hidden;
}

.editor-pane :deep(.EasyMDEContainer) {
  height: 100%;
}

.editor-pane :deep(.CodeMirror) {
  height: 100%;
  font-size: 14px;
}

.editor-pane :deep(.editor-toolbar) {
  border-top: none;
  border-left: none;
  border-right: none;
}

.preview-pane {
  flex: 1;
  overflow: auto;
  padding: 20px;
  border-left: 1px solid #eee;
  background: #fff;
  line-height: 1.8;
}

.preview-pane :deep(h1) { font-size: 24px; font-weight:600; margin: 16px 0; }
.preview-pane :deep(h2) { font-size: 20px; font-weight:600; margin: 14px 0; }
.preview-pane :deep(h3) { font-size: 16px; font-weight: 600; margin: 12px 0; }
.preview-pane :deep(p) { margin: 8px 0; }
.preview-pane :deep(li) { margin-left: 20px; list-style: disc; }
.preview-pane :deep(code) { background: #f5f5f5; padding: 2px 6px; border-radius: 3px; font-family: monospace; }
.preview-pane :deep(pre) { background: #f5f5f5; padding: 15px; border-radius: 5px; overflow-x: auto; }
.preview-pane :deep(pre code) { background: none; padding: 0; }
.preview-pane :deep(img) { max-width: 100%; }
.preview-pane :deep(a) { color: #409eff; }
</style>