# Pipeline State — Issue 5

> 子 agent 写入，主 agent 消费（提取 References 构造后续 prompt）。

## Project Context

项目: 文件管理系统 | 阶段: MVP (v1.0) | Pipeline 版本: v1
技术栈: Vue3 + Element Plus + SpringBoot + SQLite
架构: Controller → Service → Repository → Model
设计原则: 前后端不分离 | 离线优先 | 快速验证

## Task

Issue: #5
Title: 实现回收站功能
Issue 类型: 新功能
一句话描述: 删除文件进入回收站，30天后自动清除，用户可查看和恢复
用户场景: 用户误删文件后可在回收站中恢复，避免数据丢失
为什么做: 提升用户体验，防止误操作导致的数据丢失
来源: docs/issues/issue-5-回收站.md (F-TRASH-001)

### 需求来源

| 需求点 | 来源文档 | 章节 |
|--------|---------|------|
| 回收站列表展示 | docs/issues/issue-5-回收站.md | F-TRASH-001 功能需求 |
| 恢复至原目录 | docs/issues/issue-5-回收站.md | F-TRASH-001 功能需求 |
| 手动清空 | docs/issues/issue-5-回收站.md | F-TRASH-001 功能需求 |
| 30天自动清除 | docs/issues/issue-5-回收站.md | F-TRASH-001 功能需求 |

## Decisions

### ADR-1: 删除时间戳记录方案
选择: 方案 A - 扩展现有表字段（FileInfo/Directory 添加 deleteAt 字段） | 原因: MVP 阶段优先快速验证；现有模型已有 deleted 字段，添加 deleteAt 符合设计模式；MyBatis-Plus 映射简单
否决: 方案 B（新建回收站表）— MVP 阶段过早设计，增加复杂度；方案 C（使用 updatedAt）— 语义不清晰，恢复时需特殊处理
影响: 修改 FileInfo.java、Directory.java 添加 deleteAt 字段；修改 FileService.deleteFile、DirectoryService.deleteDirectory 设置 deleteAt
阶段: design

### ADR-2: 回收站 API 接口设计
选择: 方案 A - 新建 TrashController | 原因: 回收站是独立业务功能，应该独立管理；符合单一职责原则；前端已有 /api/trash 路径预期
否决: 方案 B（扩展现有 Controller）— 违反单一职责原则；方案 C（/api/recycle-bin）— 与前端已有接口不一致
影响: 新建 TrashController.java、TrashService.java；前端接口路径保持 /api/trash/*
阶段: design

### ADR-3: 定时清理任务设计
选择: 方案 A - Spring @Scheduled 定时任务 | 原因: Spring 原生支持，无需额外依赖；单机部署足够；执行时间设为凌晨2点避开高峰期
否决: 方案 B（消息队列）— MVP 阶段过早优化；方案 C（数据库定时任务）— SQLite 不支持
影响: 在 TrashService 添加 @Scheduled 方法 cleanExpiredItems()
阶段: design

## Boundaries

必须遵守:
- Controller → Service → Repository → Model 分层约束
- 软删除使用 deleted 字段（现有 model 已有）
- 响应格式统一 {code, message, data}
- 定时任务在 Service 层
- 使用 MyBatis-Plus ORM

范围之内:
- 回收站列表接口 GET /api/trash/list（文件+目录的已删除记录）
- 删除到回收站（复用现有 deleteFile/deleteDirectory 软删除）
- 恢复文件/目录（从回收站恢复到原位置）
- 永久删除（物理删除，不经过回收站）
- 清空回收站（批量物理删除）
- 30天自动清除定时任务

范围之外:
- 回收站容量限制
- 批量恢复/删除（先做单条）
- 跨用户恢复（暂不支持）
- 恢复时选择新位置（先恢复到原位置）

### 范围检查

| 范围之内 | 符合范围之外约束 |
|---------|-----------------|
| 回收站列表 | 不涉及容量限制 |
| 恢复文件 | 不涉及批量，先做单条 |
| 30天自动清除 | 定时任务在 Service 层 |

## Open Questions

🔴 阻塞: 
- 删除文件/目录时是否需要记录 delete_at 时间戳？（现有 deleteFile/deleteDirectory 只设置 deleted=1，未记录删除时间）

🟡 不阻塞: 
- 前端回收站 UI 已存在（Trash.vue），需确认后端接口是否与前端预期一致

🟢 已解决: 
- 使用 MyBatis-Plus + @TableLogic 软删除（已确定） 

## Current Stage

阶段: deliver
指令: stages/deliver.md
关注: 提交代码、创建 PR

### 产出
状态: ✅
文件:
  - file-manager/src/main/java/com/filemanager/model/FileInfo.java: 添加 deleteAt 字段
  - file-manager/src/main/java/com/filemanager/model/Directory.java: 添加 deleteAt 字段
  - file-manager/src/main/java/com/filemanager/service/FileService.java: 修改 deleteFile/deleteFiles 设置 deleteAt
  - file-manager/src/main/java/com/filemanager/service/DirectoryService.java: 修改 deleteDirectory 设置 deleteAt
  - file-manager/src/main/java/com/filemanager/service/TrashService.java: 新建，回收站核心业务逻辑
  - file-manager/src/main/java/com/filemanager/controller/TrashController.java: 新建，API 接口

### 自检
- [x] Task 1: FileInfo 添加 deleteAt 字段
- [x] Task 2: Directory 添加 deleteAt 字段
- [x] Task 3: FileService 删除时设置 deleteAt
- [x] Task 4: DirectoryService 删除时设置 deleteAt
- [x] Task 5: TrashService 回收站业务逻辑
- [x] Task 6: TrashController API 接口
- [x] Task 7: 前端 API 匹配验证
- [x] Task 8: @Scheduled 定时清理任务

### 提交信息
- Commit: 1908321
- 分支: feature/issue-5
- PR: https://github.com/liming-lihaini/ai_demo/pull/7

### 评审
状态: ⬜
轮次: 0
反馈:
执行顺序:
1. Task 1 & 2 并行（模型修改，无依赖）
2. Task 3 依赖 Task 1，Task 4 依赖 Task 2
3. Task 5 依赖 Task 3 & 4（TrashService 需要调用更新后的 Service）
4. Task 6 依赖 Task 5
5. Task 7 和 Task 8 可在 Task 5/6 完成后独立进行

### 自检
- [x] Task 粒度合适: 8个Task，每个可独立验证，涉及文件数合理
- [x] 验收条件可检查: 每条可 yes/no 回答
- [x] 无 design 遗漏: 对照 Decisions 逐项检查（ADR-1对应Task1/2/3/4，ADR-2对应Task5/6，ADR-3对应Task8）
- [x] 依赖无循环: 依赖图无环
- [x] 无 Boundaries 违反: 不涉及容量限制、批量操作、跨用户恢复
- [x] 并行标注合理: Task1/2 可并行

### 评审
状态: ⬚
轮次: 0
反馈: 

## References

> 主 agent 在 understand 完成后提取此列表，构造后续阶段的 sub-agent prompt。
> 后续阶段不通过此 section 读取文件。

- file-manager/src/main/java/com/filemanager/model/FileInfo.java — 文件实体，已有 deleted 字段，需添加 deleteAt
- file-manager/src/main/java/com/filemanager/model/Directory.java — 目录实体，已有 deleted 字段，需添加 deleteAt
- file-manager/src/main/java/com/filemanager/service/FileService.java — 文件业务逻辑，现有 deleteFile/restoreFile 方法
- file-manager/src/main/java/com/filemanager/service/DirectoryService.java — 目录业务逻辑，现有 deleteDirectory/restoreDirectory 方法
- file-manager/src/main/java/com/filemanager/controller/FileController.java — 文件 Controller（参考接口风格）
- file-manager/src/main/webapp/src/views/Trash.vue — 前端回收站页面（现有 UI）
- file-manager/src/main/webapp/src/api/request.js — 前端请求封装
- docs/issues/issue-5-回收站.md — Issue 需求文档
- docs/current/decisions/adr-recycle-bin-delete-timestamp.md — 删除时间戳记录方案 ADR
- docs/current/decisions/adr-recycle-bin-api.md — 回收站 API 接口设计 ADR
- docs/current/decisions/adr-recycle-bin-scheduled-cleanup.md — 定时清理任务设计 ADR
- docs/plans/2026-04-20-回收站功能实现计划.md — 执行计划

## 知识回流

| 阶段 | 回流类型 | 文件路径 | 状态 |
|------|---------|---------|------|
| understand | Issue 摘要 | docs/current/issues/issue-5-recycle-bin.md | ✅ |
| design | ADR | docs/current/decisions/adr-recycle-bin-delete-timestamp.md | ✅ |
| design | ADR | docs/current/decisions/adr-recycle-bin-api.md | ✅ |
| design | ADR | docs/current/decisions/adr-recycle-bin-scheduled-cleanup.md | ✅ |
| plan | 执行计划 | docs/plans/2026-04-20-回收站功能实现计划.md | ✅ |
| implement | Knowledge 更新 | docs/knowledge/{module}.md | ⏳/N/A |
| implement | Pipeline 文件修改 | stages/*.md, commands/*.md, CLAUDE.md | ⏳/N/A |

> Pipeline 文件修改通过 self-evolution.md 处理，不通过 knowledge 回流。

## Deliver

PR: https://github.com/liming-lihaini/ai_demo/pull/7
创建时间: 2026/04/20

### 验证结果
- 代码完整性检查: 通过
- 架构约束检查: 通过 (Controller → Service → Repository → Model)
- 定时任务位置: 通过 (@Scheduled 在 TrashService)
- commit message: 符合 conventional commits 格式
- PR 关联: Closes #5