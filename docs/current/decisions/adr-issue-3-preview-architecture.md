# ADR: 文件预览服务架构

## 背景

Issue-3 文件预览功能需要支持多种文件类型的在线预览：
- 直接预览：PDF、TXT、MD
- 转换预览：DOC、DOCX、XLS、XLSX、PPT、PPTX（转换为PDF）
- 图片预览：JPG、JPEG、PNG、GIF、BMP
- 音频预览：MP3、WAV
- 视频预览：MP4、AVI

项目为桌面端本地文件管理工具，使用 SpringBoot + Vue3 + SQLite 技术栈。需要确定预览服务的整体架构，包括转换方式、API 设计、前端组件结构。

## 备选方案

### 方案 A: 服务端转换 + 流式返回
- 优势: 客户端无需额外依赖，所有转换逻辑集中在服务端，便于管理和维护
- 劣势: 服务端资源消耗大，大文件转换占用服务器内存

### 方案 B: 客户端转换 + WebAssembly
- 优势: 利用客户端计算资源，减轻服务端压力；适合桌面端应用
- 劣势: 需要引入额外的客户端库，兼容性需要测试

### 方案 C: 混合方案（服务端为主 + 客户端辅助）
- 优势: 取长补短，服务端处理复杂转换（Office），客户端处理轻量级预览（TXT/MD/图片）
- 劣势: 架构复杂度增加，需要处理两种方式的协调

## 决策

选择方案 A（服务端转换 + 流式返回），因为：
1. 项目是桌面端应用，服务端和客户端在同一环境，服务端处理不影响用户体验
2. 项目已有 SpringBoot 基础设施，可以复用现有 Controller-Service-Repository 分层
3. 便于统一管理转换依赖和缓存策略
4. 避免客户端引入过多依赖，保持前端轻量

## 否决项

- 方案 B: 桌面端应用场景下，引入 WebAssembly 增加了复杂度，而服务端已有 Java 处理库
- 方案 C: MVP 阶段应保持架构简洁，混合方案适合后续优化阶段

## 影响

### 新增文件
- `file-manager/src/main/java/com/filemanager/service/PreviewService.java` - 预览服务核心逻辑
- `file-manager/src/main/java/com/filemanager/controller/PreviewController.java` - 预览接口
- `file-manager/src/main/java/com/filemanager/util/OfficeConverter.java` - Office 转 PDF 工具类
- `file-manager/src/main/webapp/src/components/PreviewDialog.vue` - 通用预览弹窗
- `file-manager/src/main/webapp/src/components/PdfViewer.vue` - PDF 预览组件
- `file-manager/src/main/webapp/src/components/ImageViewer.vue` - 图片预览组件
- `file-manager/src/main/webapp/src/components/MediaPlayer.vue` - 音视频播放组件

### 修改文件
- `file-manager/src/main/webapp/src/router/index.js` - 添加预览路由
- `file-manager/src/main/webapp/src/api/preview.js` - 添加预览 API 封装
- `pom.xml` - 添加 Apache POI 依赖（如需要）

### 影响模块
- 后端: 新增 PreviewService、PreviewController、OfficeConverter
- 前端: 新增 PreviewDialog、PdfViewer、ImageViewer、MediaPlayer 组件

## 不需要决策的维度

- 数据模型: 无需新增数据库表，预览服务直接操作文件系统
- 状态管理: 预览状态在前端组件内管理，服务端无状态
- 异步处理: 转换操作可考虑异步，但 MVP 阶段同步处理即可（需求 ≤5秒）
- 安全: 预览接口复用现有文件下载的权限校验逻辑
- 兼容性: 仅影响预览模块，不影响现有文件管理功能