# Pipeline State — Issue 6

> 子 agent 写入，主 agent 消费。

## Project Context

项目: file-manager | 阶段: MVP (v1.0) | Pipeline 版本: v1
技术栈: Vue3 + SpringBoot + SQLite | 前端: Element Plus
架构: Controller → Service → Repository → Model
设计原则: 前后端不分离 | 离线优先 | 快速验证

## Task

Issue: #6
Title: 支持全文检索功能
Issue 类型: 新功能
一句话描述: 实现文件和目录的全文检索功能，支持按文件名、内容、文件类型等维度进行快速搜索
用户场景: 用户可以在应用中快速搜索文件和目录，支持模糊匹配、内容搜索、多维度筛选
为什么做: 提升用户体验，快速定位文件
来源: MVP 功能规划

## Decisions

- 索引更新策略: 定时更新（每5分钟批量同步）
- 搜索历史存储: 前端 localStorage
- FTS5 表设计: 文件和目录分离（file_fts, directory_fts）
- API 设计: 多接口分离（/api/search, /api/search/suggest, /api/search/history）

### 否决项

- 方案 A (实时索引更新): 影响文件操作性能，MVP 阶段不优先
- 方案 A (数据库存储搜索历史): 增加后端复杂度，MVP 阶段不必要
- 方案 A (单一 FTS 表): 类型筛选效率低

## Boundaries

必须遵守:
- 遵循 CLAUDE.md 定义的架构约束
- Controller → Service → Repository → Model 分层
- 前端使用 Vue3 + Element Plus
- 使用 SQLite FTS5 实现全文检索

范围之内:
- 文件名模糊搜索
- 文件内容搜索（txt、md 等文本文件）
- 目录名称搜索
- 多维度筛选（文件类型、日期、大小）
- 搜索历史记录
- 关键词高亮

范围之外:
- 全文索引更新实时性（允许延迟）
- 搜索结果相关性算法优化
- 分布式搜索
- 搜索统计分析

## Open Questions

🟢 无（已全部解决）

## Current Stage

阶段: deliver
指令: stages/deliver.md
关注: 验收、交付

### 产出
状态: ✅
文件:
- SearchService.java
- SearchController.java
- SearchIndexService.java
- search.js
- SearchView.vue
- schema.sql (FTS5)

### 自检
- [x] 验收条件 1-10: 全部核对通过

### 评审
状态: ⬜
轮次: 0
反馈:

### 产出
状态: ✅
文件:
- schema.sql: 添加 FTS5 表和触发器
- SearchService.java: 后端搜索服务
- SearchController.java: 后端搜索接口 /api/search
- SearchIndexService.java: 定时索引同步服务
- search.js: 前端搜索 API
- SearchView.vue: 前端搜索页面

### 自检
- [x] Task 1: 数据库 FTS5 索引创建
- [x] Task 2: SearchService 后端服务
- [x] Task 3: SearchController 后端接口
- [x] Task 4: 前端搜索组件
- [x] Task 5: 前端搜索页面
- [x] Task 6: 定时索引同步

### 产出
状态: ✅
文件:
- Task: 完整理解 Issue #6 需求（10项验收条件）
- Boundaries: 已明确范围之内（6项）和范围之外（4项）
- Open Questions: 已识别 2 个待确认项

### 自检
- [x] 标准: 理解 Issue 需求 — 结果: 全文检索、FTS5索引、API设计、筛选功能均已理解
- [x] 标准: Boundaries 清晰 — 结果: 范围之内（文件名/内容搜索、多维筛选、历史记录、高亮）/ 范围之外（实时性、相关性算法、分布式搜索、统计分析）
- [x] 标准: Open Questions 识别 — 结果: 索引更新策略（实时/定时）、搜索历史存储方式（数据库/本地）

### 评审
状态: ⬜
轮次: 0
反馈:

## References

- docs/issues/issue-6-支持全文检索功能.md: Issue 描述
- docs/current/decisions/adr-search-index-strategy.md: 索引更新策略 ADR
- docs/current/decisions/adr-search-history-storage.md: 搜索历史存储 ADR
- docs/current/decisions/adr-search-fts-tables.md: FTS5 表设计 ADR
- docs/current/decisions/adr-search-api.md: API 设计 ADR
- file-manager/src/main/java/com/filemanager/model/FileInfo.java: 文件模型
- file-manager/src/main/java/com/filemanager/model/Directory.java: 目录模型
- file-manager/src/main/java/com/filemanager/service/FileService.java: 文件服务
- file-manager/db/schema.sql: 数据库结构

## 知识回流

| 阶段 | 回流类型 | 文件路径 | 状态 |
|------|---------|---------|------|
| understand | Issue 摘要 | docs/current/issues/issue-6-full-text-search.md | ⏳ |
| design | ADR | docs/current/decisions/adr-*.md | ⏳ |
| plan | 实现计划 | docs/plans/*.md | ⏳ |
| implement | Knowledge 更新 | docs/knowledge/*.md | N/A |
| implement | Pipeline 文件修改 | stages/*.md | N/A |

## Understand

创建时间: 2026/04/20
