# ADR: Markdown 编辑器技术方案

## 背景

Issue #4 要求实现 Markdown 文件的在线编辑功能，支持实时预览、语法高亮和导出。
现有代码分析：
- `Editor.vue` 使用 Milkdown 编辑器但实现不完整（保存/导出逻辑缺失）
- `package.json` 已安装 easymde 依赖
- `TextViewer.vue` 包含简单的 Markdown 渲染逻辑
- 后端 `FileService` 已支持文件读写

## 备选方案

### 方案 A: EasyMDE (easymde)
- 优势: 项目已安装，开箱即用；功能完整（预览/工具栏/快捷键）
- 劣势: UI 样式定制需要额外 CSS
- 优势: 成熟稳定，文档完善

### 方案 B: CodeMirror 6
- 优势: 功能强大，语法高亮精准
- 劣势: 需要额外安装和配置，集成复杂度高

### 方案 C: Milkdown (已有引用但实现不完整)
- 优势: 设计美观
- 劣势: 实现不完整，缺少保存/导出逻辑

## 决策

选择方案 A (EasyMDE)，因为：
1. `package.json` 已安装，无需引入新依赖
2. 功能完整，包含实时预览和导出能力
3. 集成简单，降低实现风险

### 后端方案

### 方案 A: 扩展 FileService
- 优势: 复用现有 FileService 代码，侵入最小
- 劣势: 单文件操作需要新建 API 端点

### 方案 B: 新建 MarkdownService
- 优势: 职责分离清晰
- 劣势: 增加代码量，MVP 阶段过早设计

## 决策

选择方案 A (扩展 FileService)，因为 MVP 阶段优先复用现有代码。

## 影响

- 修改 `Editor.vue`: 替换为 EasyMDE 实现
- 扩展 `FileController`: 添加更新文件内容 API
- 复用 `FileService.updateContent` 方法

### 否决项

- Milkdown: 实现不完整，缺少保存导出逻辑
- CodeMirror 6: 需要额外依赖，集成复杂度高

### 需要决策的维度

| 维度 | 决策 |
|------|------|
| 数据模型 | 复用现有 FileInfo，无需新增字段 |
| API 设计 | 添加 `/api/file/update-content` 端点 |
| 架构分层 | 在 FileService 添加 updateContent 方法 |
| 状态管理 | 无需状态管理，文件内容直接落库 |
| 安全 | 无安全考量，内部工具 |
| 可观测性 | 无监控需求 |
| 兼容性 | 不影响现有功能 |