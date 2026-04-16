<template>
  <div class="editor-view">
    <div class="toolbar">
      <el-button @click="handleSave">
        <el-icon><FolderChecked /></el-icon>
        保存
      </el-button>
      <el-button @click="handleExport('md')">
        导出MD
      </el-button>
      <el-button @click="handleExport('pdf')">
        导出PDF
      </el-button>
      <el-button @click="handleExport('html')">
        导出HTML
      </el-button>
    </div>
    <div class="editor-container">
      <div class="editor-pane">
        <div id="editor"></div>
      </div>
      <div class="preview-pane" v-html="previewContent"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Editor, root } from 'milkdown'
import { commonmark } from '@milkdown/preset-commonmark'
import { nord } from '@milkdown/theme-nord'
import request from '../api/request'

const route = useRoute()
const router = useRouter()
const previewContent = ref('')
const fileId = ref(route.query.id)

const initEditor = async () => {
  const editor = await Editor.make()
    .config(theme, nord)
    .use(commonmark)
    .use(Editor)
    .create()

  document.getElementById('editor').appendChild(editor)
}

const handleSave = async () => {
  // TODO: 保存
  router.push('/files')
}

const handleExport = (format) => {
  // TODO: 导出
}

onMounted(() => {
  initEditor()
})
</script>

<style scoped>
.editor-view {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.toolbar {
  padding: 10px;
  border-bottom: 1px solid #eee;
}
.toolbar .el-button {
  margin-right: 10px;
}
.editor-container {
  flex: 1;
  display: flex;
}
.editor-pane,
.preview-pane {
  flex: 1;
  overflow: auto;
  padding: 20px;
}
.preview-pane {
  border-left: 1px solid #eee;
}
</style>