# ADR: 预览前端组件设计

## 背景

Issue-3 文件预览功能需要前端组件支持多种文件类型的预览。根据需求，需要设计组件结构和交互方式。

项目前端使用 Vue3 + Element Plus，已有组件目录: `src/components/`、`src/views/`。

## 备选方案

### 方案 A: 单一通用预览弹窗 + 内置组件
- 优势: 统一入口，代码复用度高；用户操作一致
- 劣势: 组件内部逻辑可能较复杂

### 方案 B: 多个独立预览组件
- 优势: 每个组件职责单一，易于维护
- 劣势: 需要管理多个组件的切换

### 方案 C: 组合方案（预览弹窗 + 类型化内容组件）
- 优势: 弹窗统一管理头部/关闭/全屏，内容组件专注渲染
- 劣势: 需要定义组件通信协议

## 决策

选择方案 C（组合方案），因为：
1. 弹窗的通用行为（关闭、全屏、下载）可以统一处理
2. 内容组件独立，便于单独测试和维护
3. 符合 Vue 组件化设计原则

## 组件结构设计

```
PreviewDialog (通用预览弹窗)
├── Header: 标题 + 操作按钮（下载/全屏/关闭）
├── Content: 根据类型加载对应组件
│   ├── PdfViewer (PDF 预览)
│   ├── TextViewer (TXT/MD 预览)
│   ├── ImageViewer (图片预览 + 缩放/旋转)
│   └── MediaPlayer (音频/视频播放)
└── Footer: 类型特定操作（如有）
```

### 组件清单

| 组件 | 路径 | 职责 |
|------|------|------|
| PreviewDialog | components/PreviewDialog.vue | 通用弹窗容器，处理全屏/下载/关闭 |
| PdfViewer | components/PdfViewer.vue | PDF 渲染，使用 pdf.js |
| TextViewer | components/TextViewer.vue | TXT/MD 文本预览，MD 支持实时编辑 |
| ImageViewer | components/ImageViewer.vue | 图片预览，支持缩放/旋转/切换 |
| MediaPlayer | components/MediaPlayer.vue | 音视频播放，HTML5 Video/Audio |

### 组件通信

```
PreviewDialog
├── props: fileId, fileName, fileType, visible
├── emits: close, download
└── slots: default (加载内容组件)

内容组件
├── props: fileId, url
└── emits: loadError, loadComplete
```

## 否决项

- 方案 A: 单一组件内部逻辑过于复杂，不利于后续维护
- 方案 B: 多个独立组件导致重复的弹窗逻辑

## 影响

### 新增文件
- `file-manager/src/main/webapp/src/components/PreviewDialog.vue`
- `file-manager/src/main/webapp/src/components/PdfViewer.vue`
- `file-manager/src/main/webapp/src/components/TextViewer.vue`
- `file-manager/src/main/webapp/src/components/ImageViewer.vue`
- `file-manager/src/main/webapp/src/components/MediaPlayer.vue`

### 新增依赖（package.json）
```json
{
  "pdfjs-dist": "^4.0.379"  // PDF 预览
}
```

### 使用方式

```vue
<!-- 在文件列表页面使用 -->
<PreviewDialog
  v-model:visible="previewVisible"
  :file-id="currentFile.id"
  :file-name="currentFile.name"
  :file-type="currentFile.format"
/>
```

## 不需要决策的维度

- 路由: 预览使用弹窗方式，不需要独立路由
- 状态管理: 预览状态在组件内管理，不需要全局 Store