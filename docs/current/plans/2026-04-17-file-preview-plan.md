# 执行计划: 文件预览功能开发

## Task 1: 添加 Apache POI 依赖

做什么: 在 pom.xml 中添加 Apache POI 和 PDFBox 依赖（PDFBox 已有）

涉及文件:
- file-manager/pom.xml

预估变更: 约 30 行

验收条件:
- `mvn dependency:tree | grep poi` 输出包含 poi 和 poi-ooxml
- 项目 `mvn compile` 成功

依赖: 无

## Task 2: 创建 OfficeConverter 工具类

做什么: 实现 Office 文档（DOC/DOCX/XLS/XLSX/PPT/PPTX）转换为 PDF 的工具类

涉及文件:
- file-manager/src/main/java/com/filemanager/util/OfficeConverter.java（新建）

预估变更: 约 200 行

验收条件:
- OfficeConverter 类编译通过
- convertToPdf(InputStream, String format) 方法存在且返回 PDF bytes
- 支持的格式: doc, docx, xls, xlsx, ppt, pptx

依赖: Task 1

## Task 3: 创建 PreviewService 业务逻辑层

做什么: 实现预览服务核心逻辑，包括文件预览信息获取、内容返回、缩略图生成

涉及文件:
- file-manager/src/main/java/com/filemanager/service/PreviewService.java（新建）

预估变更: 约 250 行

验收条件:
- getPreviewInfo(Long fileId) 方法返回预览信息（含 previewType, url, needConvert）
- getPreviewContent(Long fileId) 返回文件内容流
- getPdfPreview(Long fileId) 处理 PDF 或转换后 PDF
- getThumbnail(Long fileId) 返回缩略图（图片/视频）
- 集成 OfficeConverter 实现转换

依赖: Task 2

## Task 4: 创建 PreviewController 接口层

做什么: 实现预览 API 接口

涉及文件:
- file-manager/src/main/java/com/filemanager/controller/PreviewController.java（新建）

预估变更: 约 150 行

验收条件:
- GET /api/preview/{fileId} 返回预览信息 JSON
- GET /api/preview/content/{fileId} 返回文件内容流
- GET /api/preview/pdf/{fileId} 返回 PDF 内容
- GET /api/preview/thumb/{fileId} 返回缩略图
- 所有接口返回格式符合 {code, message, data}

依赖: Task 3

## Task 5: 添加前端 PDF.js 依赖

做什么: 在 package.json 中添加 pdfjs-dist 依赖

涉及文件:
- file-manager/src/main/webapp/package.json

预估变更: 约 5 行

验收条件:
- package.json 包含 pdfjs-dist 依赖
- `npm install` 成功

依赖: 无

## Task 6: 创建预览 API 封装

做什么: 创建前端预览 API 封装

涉及文件:
- file-manager/src/main/webapp/src/api/preview.js（新建）

预估变更: 约 80 行

验收条件:
- getPreviewInfo(fileId) 方法存在
- getPreviewContent(fileId) 方法存在
- getPdfPreview(fileId) 方法存在
- getThumbnail(fileId) 方法存在

依赖: Task 5

## Task 7: 创建 PreviewDialog 通用预览弹窗组件

做什么: 创建通用预览弹窗组件，处理全屏/下载/关闭等通用逻辑

涉及文件:
- file-manager/src/main/webapp/src/components/PreviewDialog.vue（新建）

预估变更: 约 150 行

验收条件:
- 组件支持 visible, fileId, fileName, fileType props
- 支持 fullscreen, download, close 事件
- 根据 fileType 加载对应内容组件

依赖: Task 6

## Task 8: 创建 PdfViewer PDF 预览组件

做什么: 创建 PDF 预览组件，使用 pdf.js 渲染

涉及文件:
- file-manager/src/main/webapp/src/components/PdfViewer.vue（新建）

预估变更: 约 200 行

验收条件:
- 组件支持 url, fileId props
- 显示 PDF 页面（支持多页）
- 支持 zoom in/out, page navigation
- 大文件（>=20MB）显示加载进度

依赖: Task 7

## Task 9: 创建 TextViewer 文本预览组件

做什么: 创建 TXT/MD 文本预览组件，MD 支持渲染

涉及文件:
- file-manager/src/main/webapp/src/components/TextViewer.vue（新建）

预估变更: 约 120 行

验收条件:
- 组件支持 url, fileId props
- TXT 文件直接显示文本
- MD 文件渲染为 HTML（支持预览）
- 大文件（>=20MB）显示加载进度

依赖: Task 7

## Task 10: 创建 ImageViewer 图片预览组件

做什么: 创建图片预览组件，支持缩放/旋转/切换

涉及文件:
- file-manager/src/main/webapp/src/components/ImageViewer.vue（新建）

预估变更: 约 180 行

验收条件:
- 组件支持 images（数组）, initialIndex props
- 支持 zoom in/out
- 支持 rotate left/right
- 支持 prev/next 切换
- 大文件显示加载进度

依赖: Task 7

## Task 11: 创建 MediaPlayer 音视频播放组件

做什么: 创建音视频播放组件，支持播放控制

涉及文件:
- file-manager/src/main/webapp/src/components/MediaPlayer.vue（新建）

预估变更: 约 150 行

验收条件:
- 组件支持 url, fileId, type (audio/video) props
- 支持 play/pause
- 支持 volume control
- 支持 seek（进度拖拽）
- 视频支持 fullscreen

依赖: Task 7

## Task 12: 集成预览组件到文件列表页面

做什么: 在 FileManager.vue 中集成预览功能，双击文件打开预览

涉及文件:
- file-manager/src/main/webapp/src/views/FileManager.vue（修改）

预估变更: 约 50 行

验收条件:
- 双击文件触发预览
- 根据文件类型打开对应的 PreviewDialog
- 文件类型映射正确（pdf->PdfViewer, image->ImageViewer, etc.）

依赖: Task 7, Task 8, Task 9, Task 10, Task 11

## 可并行的 Task

- Task 1, Task 5 可并行（依赖不同项目文件）
- Task 2 依赖 Task 1
- Task 3 依赖 Task 2
- Task 4 依赖 Task 3
- Task 6 依赖 Task 5
- Task 7 依赖 Task 6
- Task 8, Task 9, Task 10, Task 11 可并行（各自独立组件）
- Task 12 依赖 Task 7-11

## 依赖图

```
Task 1 (POI依赖) ──────┐
                       ▼
Task 2 (OfficeConverter) ──► Task 3 (PreviewService) ──► Task 4 (PreviewController)
                                                       │
Task 5 (PDF.js依赖) ───────────────────────────────► Task 6 (preview.js API)
                                                       │
                                                       ▼
                                            Task 7 (PreviewDialog)
                                                      │
                        ┌─────────────────────────────┼─────────────────────┐
                        ▼                             ▼                     ▼
                   Task 8 (PdfViewer)        Task 9 (TextViewer)    Task 10 (ImageViewer)
                        │                             │                     │
                        │                             │                     ▼
                        │                             │               Task 11 (MediaPlayer)
                        │                             │                     │
                        └─────────────────────────────┴─────────────────────┘
                                                      │
                                                      ▼
                                            Task 12 (集成到页面)
```

## 不做的事情

- 断点续传: MVP 阶段不需要，Task 12 只需实现基本预览功能
- 预览权限控制: 复用现有文件下载的权限校验逻辑
- 预览缓存策略: MVP 阶段直接读取文件系统
- 异步转换: MVP 阶段同步处理（需求 ≤5秒）
- MD 编辑模式: MVP 阶段仅实现预览，不实现编辑功能