# ADR: 回收站 API 接口设计

## 背景

Issue #5 需要实现回收站功能，后端需要提供相应 API 接口。前端 Trash.vue 已存在，期望以下接口：
- GET /api/trash - 获取回收站列表
- POST /api/trash/{id}/restore - 恢复文件/目录
- DELETE /api/trash/{id} - 彻底删除
- DELETE /api/trash/clear - 清空回收站

## 备选方案

### 方案 A: 新建 TrashController
- 优势: 职责分离，回收站相关接口集中管理；符合单一职责原则；易于维护和扩展
- 劣势: 需要新建 Controller 类

### 方案 B: 扩展现有 FileController 和 DirectoryController
- 优势: 无需新建文件；现有代码结构保持不变
- 劣势: FileController 和 DirectoryController 职责会变得复杂；不符合单一职责原则；后续维护困难

### 方案 C: 使用 RESTful 资源风格 /api/recycle-bin
- 优势: 语义更清晰，"回收站"作为独立资源
- 劣势: 与前端已有调用路径不一致，需要修改前端

## 决策

选择方案 A（新建 TrashController），因为：
1. 回收站是独立业务功能，应该独立管理
2. 符合单一职责原则，Controller 只做参数校验和响应格式化
3. 便于后续扩展回收站相关功能（如统计、配额等）
4. 前端已有 /api/trash 路径预期，保持一致性

## 否决项

- 方案 B: 违反单一职责原则，Controller 会变得臃肿
- 方案 C: 与前端已有接口不一致，需要额外修改前端代码

## 影响

- 新增文件: file-manager/src/main/java/com/filemanager/controller/TrashController.java
- 新增文件: file-manager/src/main/java/com/filemanager/service/TrashService.java（回收站业务逻辑）
- 新增文件: file-manager/src/main/java/com/filemanager/repository/TrashRepository.java（如需要）或复用现有 Repository
- 对下游的影响: plan 阶段需要根据此决策设计具体接口路径和参数

## 不需要决策的维度

- 架构分层: 已遵循 Controller → Service → Repository → Model 约束
- 状态管理: 不需要决策，使用数据库查询
- 异步处理: 不需要决策，同步处理即可
- 安全: 不需要决策，仅内部接口无外部暴露