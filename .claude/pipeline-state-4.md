# Pipeline State — Issue 4

> 子 agent 写入，主 agent 消费。

## Project Context

项目: file-manager | 阶段: MVP (v1.0) | Pipeline 版本: v1
技术栈: Vue3 + SpringBoot + SQLite | 前端: Element Plus
架构: Controller → Service → Repository → Model
设计原则: 前后端不分离 | 离线优先 | 快速验证

## Task

Issue: #4
Title: MD文件编辑功能
Issue 类型: 新功能
一句话描述: 实现Markdown文件的在线编辑功能，支持实时预览、语法高亮和导出
用户场景: 用户可以在应用内直接编辑Markdown文件，无需使用外部编辑器，并支持实时预览渲染效果
为什么做: 完善文件预览功能，提供一站式的文档编辑体验
来源: MVP 功能规划

## Decisions

- 编辑器技术方案: 选择 easymde，因为 package.json 已安装，集成简单
- 后端方案: 扩展 FileService，添加 updateContent 方法

### 否决项

- Milkdown: 实现不完整，缺少保存导出逻辑
- CodeMirror 6: 需要额外依赖，集成复杂度高

## Boundaries

必须遵守:
- 遵循 CLAUDE.md 定义的架构约束
- Controller → Service → Repository → Model 分层
- 前端使用 Vue3 + Element Plus

范围之内:
- Markdown 文件在线编辑
- 实时预览
- 语法高亮
- 导出功能

范围之外:
- 多人协作文档编辑
- 版本历史（v1.0）

## Open Questions

🟡 不阻塞:
- 使用哪种 Markdown 编辑器库？（已确定使用 easymde，因 package.json 已安装）

## Current Stage

阶段: deliver
指令: stages/deliver.md
关注: Task, Boundaries, 实现产出

### 产出
状态: ✅
文件: 
- Editor.vue: 重构为 EasyMDE 编辑器
- FileService.java: 添加 updateContent 方法
- FileController.java: 添加 update-content API
- fileContent.js: 前端 API 封装

### 自检
- [x] 标准: 理解 Issue 需求 — 结果: 已识别现有 Editor.vue 实现和 TextViewer.vue 组件
- [x] 标准: Boundaries 清晰 — 结果: 范围之内/之外已明确
- [x] Open Questions 识别 — 结果: 识别出编辑器库选择问题

### 评审
状态: ⬜
轮次: 0
反馈:

## References

- file-manager/src/main/webapp/src/views/Editor.vue: 现有 MD 编辑器实现
- file-manager/src/main/webapp/src/components/TextViewer.vue: 文本查看器
- file-manager/src/main/webapp/package.json: 前端依赖配置
- docs/current/issues/adr-issue-4-md-editor.md: 技术方案 ADR

## 知识回流

| 阶段 | 回流类型 | 文件路径 | 状态 |
|------|---------|---------|------|
| understand | Issue 摘要 | docs/current/issues/issue-4-md-editor.md | ⏳ |
| design | ADR | docs/current/decisions/adr-issue-4-md-editor.md | ⏳ |
| implement | Knowledge 更新 | docs/knowledge/*.md | N/A |
| implement | Pipeline 文件修改 | stages/*.md | N/A |

## Deliver

PR: N/A
创建时间: 2026/04/17