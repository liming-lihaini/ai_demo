# Pipeline State — Issue 5

> 子 agent 写入，主 agent 消费。

## Project Context

项目: file-manager | 阶段: MVP (v1.0) | Pipeline 版本: v1
技术栈: Vue3 + SpringBoot + SQLite | 前端: Element Plus
架构: Controller → Service → Repository → Model
设计原则: 前后端不分离 | 离线优先 | 快速验证

## Task

Issue: #5
Title: 回收站功能
Issue 类型: 新功能
一句话描述: 实现文件回收站功能，支持删除、恢复、永久删除和30天自动清理
用户场景: 用户删除文件/目录后，可以从回收站恢复或彻底删除，超过30天的项目自动清除
为什么做: 提供数据安全和误删恢复能力
来源: MVP 功能规划

## Decisions

- 删除时间戳方案: 在 FileInfo 和 Directory 模型添加 deleteAt 字段
- API 设计: 新建 TrashController，路径 /api/trash
- 定时清理: 使用 Spring @Scheduled，每天凌晨2点执行

### 否决项

- 方案 B (新建回收站记录表): MVP 阶段过早设计，增加复杂度
- 方案 C (使用 updatedAt): 语义不清晰

## Boundaries

必须遵守:
- 遵循 CLAUDE.md 定义的架构约束
- Controller → Service → Repository → Model 分层
- 前端使用 Vue3 + Element Plus

范围之内:
- 软删除 + deleteAt 时间戳记录
- 回收站列表/恢复/永久删除
- 30天自动清理

范围之外:
- 回收站容量限制
- 批量恢复/删除
- 跨用户恢复
- 恢复时选择新位置

## Open Questions

🟢 无

## Current Stage

阶段: implement
指令: stages/implement.md
关注: Task, Boundaries, 实现产出

### 产出
状态: ✅
文件:
- FileInfo.java: 添加 deleteAt 字段
- Directory.java: 添加 deleteAt 字段
- FileService.java: 修改 deleteFile/deleteFiles 设置 deleteAt
- DirectoryService.java: 修改 deleteDirectory 设置 deleteAt
- TrashService.java: 新建，回收站核心业务逻辑
- TrashController.java: 新建，API 接口

### 自检
- [x] Task 1: FileInfo 添加 deleteAt 字段
- [x] Task 2: Directory 添加 deleteAt 字段
- [x] Task 3: FileService 删除时设置 deleteAt
- [x] Task 4: DirectoryService 删除时设置 deleteAt
- [x] Task 5: TrashService 回收站业务逻辑
- [x] Task 6: TrashController API 接口
- [x] Task 7: 前端 API 匹配验证
- [x] Task 8: @Scheduled 定时清理任务

### 评审
状态: ⬜
轮次: 0
反馈:

## References

- docs/plans/2026-04-20-回收站功能实现计划.md
- docs/current/decisions/adr-recycle-bin-delete-timestamp.md
- docs/current/decisions/adr-recycle-bin-api.md
- docs/current/decisions/adr-recycle-bin-scheduled-cleanup.md
- file-manager/src/main/java/com/filemanager/model/FileInfo.java
- file-manager/src/main/java/com/filemanager/model/Directory.java
- file-manager/src/main/java/com/filemanager/service/FileService.java
- file-manager/src/main/java/com/filemanager/service/DirectoryService.java

## 知识回流

| 阶段 | 回流类型 | 文件路径 | 状态 |
|------|---------|---------|------|
| understand | Issue 摘要 | docs/current/issues/issue-5-recycle-bin.md | ⏳ |
| design | ADR | docs/current/decisions/adr-recycle-bin-*.md | ✅ |
| implement | Knowledge 更新 | docs/knowledge/*.md | N/A |
| implement | Pipeline 文件修改 | stages/*.md | N/A |

## Implement

创建时间: 2026/04/20
