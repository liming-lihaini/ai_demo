# Pipeline State — Issue-3 文件预览

> 子 agent 写入，主 agent 消费（提取 References 构造后续 prompt）。

## Project Context

项目: 文件管理系统 | 阶段: MVP (v1.0) | Pipeline 版本: v1
技术栈: Java (SpringBoot) + Vue3 + Element Plus + SQLite
架构: Controller → Service → Repository → Model
设计原则: 前后端不分离 | 离线优先 | 快速验证

## Task

Issue: #3
Title: 文件预览功能开发
Issue 类型: 新功能
一句话描述: 实现文档、图片、音频、视频的在线预览功能
用户场景: 用户双击文件时可在线预览内容，无需下载到本地查看
为什么做: 提升用户体验，支持直接查看文件内容
来源: docs/issues/issue-3-文件预览.md (F-PREV-001~004)

## Decisions

### 预览服务架构
选择: 服务端转换 + 流式返回 | 原因: 桌面端应用场景，服务端处理不影响用户体验；复用现有 SpringBoot 基础设施
影响: 新增 PreviewService、PreviewController、OfficeConverter
ADR: docs/current/decisions/adr-issue-3-preview-architecture.md
阶段: 设计阶段确定

### Office转换方案
选择: Apache POI + PDFBox（纯Java方案） | 原因: MVP阶段优先保证可部署性，避免外部环境依赖
影响: pom.xml 新增 Apache POI 依赖；OfficeConverter 工具类
ADR: docs/current/decisions/adr-issue-3-office-conversion.md
阶段: 设计阶段确定

### 预览API设计
选择: 集中式 PreviewController | 原因: 预览逻辑统一管理，降低维护成本
影响: 新增 /api/preview/* 接口
ADR: docs/current/decisions/adr-issue-3-preview-api.md
阶段: 设计阶段确定

### 前端组件设计
选择: 组合方案（PreviewDialog + 内容组件） | 原因: 弹窗统一处理通用行为，内容组件独立便于维护
影响: 新增 PreviewDialog、PdfViewer、TextViewer、ImageViewer、MediaPlayer 组件
ADR: docs/current/decisions/adr-issue-3-frontend-components.md
阶段: 设计阶段确定

### 大文件处理策略
选择: 分块流式传输 + 前端进度显示 | 原因: HTTP Range 原生支持，实现相对简单
影响: PreviewController 支持 Range 请求；前端显示加载进度
ADR: docs/current/decisions/adr-issue-3-large-file-handling.md
阶段: 设计阶段确定

### 执行顺序决策
执行计划: docs/current/plans/2026-04-17-file-preview-plan.md
顺序说明:
- 后端: Task1(依赖) → Task2 → Task3 → Task4
- 前端: Task5(依赖) → Task6 → Task7 → Task8~11 → Task12
- Task1 和 Task5 可并行
阶段: 计划阶段确定

## Boundaries

必须遵守:
- CLAUDE.md 中定义的架构约束（Controller → Service → Repository → Model）
- 前端组件使用 Vue3 + Element Plus
- 响应格式统一 {code, message, data}
- 后端约束：Service层事务、Controller层参数校验、@Valid注解
- 前端约束：scoped styles + BEM命名、API统一封装

范围之内:
- PDF/TXT/MD 直接预览（前端实现）
- doc/docx/xls/xlsx/ppt/pptx 转换后预览（后端LibreOffice转换）
- jpg/jpeg/png/gif/bmp 图片预览
- mp3/wav 音频播放
- mp4/avi 视频播放
- ≥20MB大文件加载进度显示

范围之外:
- 在线编辑功能（MD编辑在issue-4中）
- Office文件在线编辑
- 多人协作预览
- 云端存储集成
- 视频缩略图生成（Thumbnails组件）

## Open Questions

🟢 已解决:
- PDF预览使用pdf.js（H5 Video/Audio + HTML5原生方案）
- Office转换方案已确定（Apache POI + PDFBox）
- 大文件进度显示前端统一实现
- 图片旋转前端实现

🔴 阻塞:
- 无

🟡 不阻塞:
- 无

## Current Stage

阶段: implement
指令: stages/implement.md
关注: 12个Task全部实现完成

### 产出
状态: ✅
文件:
  - Task 1: file-manager/pom.xml - 添加 Apache POI 依赖
  - Task 2: file-manager/src/main/java/com/filemanager/util/OfficeConverter.java - Office文档转换工具
  - Task 3: file-manager/src/main/java/com/filemanager/service/PreviewService.java - 预览服务
  - Task 4: file-manager/src/main/java/com/filemanager/controller/PreviewController.java - 预览接口
  - Task 5: file-manager/src/main/webapp/package.json - 添加 pdfjs-dist 依赖
  - Task 6: file-manager/src/main/webapp/src/api/preview.js - 预览API封装
  - Task 7: file-manager/src/main/webapp/src/components/PreviewDialog.vue - 预览弹窗
  - Task 8: file-manager/src/main/webapp/src/components/PdfViewer.vue - PDF预览组件
  - Task 9: file-manager/src/main/webapp/src/components/TextViewer.vue - 文本预览组件
  - Task 10: file-manager/src/main/webapp/src/components/ImageViewer.vue - 图片预览组件
  - Task 11: file-manager/src/main/webapp/src/components/MediaPlayer.vue - 音视频播放组件
  - Task 12: file-manager/src/main/webapp/src/views/FileManager.vue - 集成预览功能

### 自检
- [x] 12个Task全部实现完成
- [x] 后端: pom.xml添加POI依赖 → OfficeConverter → PreviewService → PreviewController
- [x] 前端: package.json添加pdfjs-dist → preview.js API → PreviewDialog → 4个内容组件 → 集成到页面
- [x] 文件位置符合项目结构

### 评审
状态: ⏳
轮次: 0
反馈: N/A

## References

- docs/issues/issue-3-文件预览.md: Issue需求文档
- CLAUDE.md: 架构约束和技术栈定义
- file-manager/src/main/java/com/filemanager/model/FileInfo.java: 文件实体
- file-manager/src/main/java/com/filemanager/model/Directory.java: 目录实体
- file-manager/src/main/webapp/src/views/Home.vue: 首页视图（参考现有组件结构）
- file-manager/src/main/webapp/src/views/Editor.vue: 编辑器视图（参考组件实现）
- docs/current/decisions/adr-issue-3-preview-architecture.md: 预览服务架构ADR
- docs/current/decisions/adr-issue-3-office-conversion.md: Office转换方案ADR
- docs/current/decisions/adr-issue-3-preview-api.md: API设计ADR
- docs/current/decisions/adr-issue-3-frontend-components.md: 前端组件ADR
- docs/current/decisions/adr-issue-3-large-file-handling.md: 大文件处理ADR
- docs/current/plans/2026-04-17-file-preview-plan.md: 执行计划

## 知识回流

| 阶段 | 回流类型 | 文件路径 | 状态 |
|------|---------|---------|------|
| understand | Issue 摘要 | docs/current/issues/issue-3-{title}.md | ✅ |
| design | ADR 决策 | docs/current/decisions/adr-issue-3-*.md | ✅ |
| plan | 执行计划 | docs/current/plans/2026-04-17-file-preview-plan.md | ✅ |

## Deliver

PR: N/A
创建时间: 2026/04/17