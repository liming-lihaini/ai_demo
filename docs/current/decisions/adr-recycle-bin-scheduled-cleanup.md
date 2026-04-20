# ADR: 回收站定时清理任务设计

## 背景

Issue #5 要求实现30天自动清除功能，即超过30天的已删除文件/目录应被物理删除。
根据 CLAUDE.md 架构约束，定时任务必须在 Service 层实现。

## 备选方案

### 方案 A: Spring @Scheduled 定时任务
- 优势: Spring 原生支持，无需额外依赖；可使用 cron 表达式灵活配置执行时间；与现有 Service 层集成方便
- 劣势: 单机部署，任务只在单节点执行

### 方案 B: 使用消息队列 + 消费者
- 优势: 支持分布式处理；可扩展性强
- 劣势: MVP 阶段引入额外复杂度；需要额外基础设施

### 方案 C: 使用数据库定时任务（SQLite 不支持）
- 劣势: SQLite 不支持原生定时任务

## 决策

选择方案 A（Spring @Scheduled 定时任务），因为：
1. MVP 阶段使用 Spring Boot 原生 @Scheduled 足够满足需求
2. 项目当前为单机部署，不需要分布式处理
3. Spring @Scheduled 是定时任务的最简方案，无需引入额外依赖
4. 执行时间设置为凌晨2点（避开业务高峰期）

## 否决项

- 方案 B: MVP 阶段过早优化，引入消息队列增加运维复杂度
- 方案 C: SQLite 不支持原生定时任务

## 影响

- 修改文件: file-manager/src/main/java/com/filemanager/service/TrashService.java（添加 @Scheduled 方法）
- 新增方法: cleanExpiredItems() - 查询并物理删除超过30天的已删除记录
- 对下游的影响: implement 阶段需要实现此定时任务逻辑

## 不需要决策的维度

- 异步处理: 不需要决策，@Scheduled 本身就是异步执行
- 状态管理: 不需要决策，使用数据库查询判断是否过期
- 安全: 不需要决策，仅物理删除过期数据
- 兼容性: 不需要决策，仅影响回收站内数据