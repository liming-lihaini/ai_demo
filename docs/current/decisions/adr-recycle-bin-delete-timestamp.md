# ADR: 回收站删除时间戳记录方案

## 背景

Issue #5 要求实现回收站功能，其中30天自动清除需要依据删除时间判断是否过期。
现有 FileInfo 和 Directory 模型只有 `deleted` 字段（标记软删除），没有记录删除时间。

Open Questions 中标识为 🔴 阻塞项：删除文件/目录时是否需要记录 delete_at 时间戳？

## 备选方案

### 方案 A: 扩展现有表字段
- 优势: 现有 FileInfo.java 和 Directory.java 已存在，只需添加 deleteAt 字段；使用 MyBatis-Plus 可直接映射；查询时可通过 deleted=1 AND deleteAt < threshold 快速筛选
- 劣势: 需要修改现有表结构（ALTER TABLE）；deleteAt 在 deleted=1 时才有意义，可能造成字段冗余

### 方案 B: 新建回收站记录表
- 优势: 职责分离，回收站记录与原数据解耦；可以记录更多回收站相关信息（如原路径、删除原因等）
- 劣势: 增加数据冗余（文件/目录信息需要在两张表中同时存在）；实现复杂度更高，需要维护数据一致性

### 方案 C: 使用现有 updatedAt 字段
- 优势: 无需修改表结构；现有 updatedAt 在删除时会更新为删除时间
- 劣势: updatedAt 语义不清晰（既表示更新时间又表示删除时间）；恢复时需要特殊处理（不能恢复为删除时的时间）；无法区分"更新"和"删除"两种操作

## 决策

选择方案 A（扩展现有表字段），因为：
1. 项目当前为 MVP 阶段，优先快速验证
2. 现有 FileInfo 和 Directory 已有 deleted 字段，添加 deleteAt 字段符合现有设计模式
3. 使用 MyBatis-Plus，字段映射简单
4. 查询逻辑清晰：`deleted=1 AND delete_at < now() - 30 days`

## 否决项

- 方案 B: MVP 阶段过早设计，新建表增加复杂度且造成数据冗余
- 方案 C: updatedAt 语义不清晰，恢复操作时需要特殊处理，不符合清晰性原则

## 影响

- 修改文件: file-manager/src/main/java/com/filemanager/model/FileInfo.java（添加 deleteAt 字段）
- 修改文件: file-manager/src/main/java/com/filemanager/model/Directory.java（添加 deleteAt 字段）
- 修改文件: file-manager/src/main/java/com/filemanager/service/FileService.java（deleteFile 方法需设置 deleteAt）
- 修改文件: file-manager/src/main/java/com/filemanager/service/DirectoryService.java（deleteDirectory 方法需设置 deleteAt）
- 对下游的影响: plan 阶段需要根据此决策设计数据库迁移脚本

## 不需要决策的维度

- 状态管理: 不需要决策，使用数据库字段存储
- 异步处理: 不需要决策，同步处理即可
- 安全: 不需要决策，仅内部接口无外部暴露
- 兼容性: 不需要决策，MVP 阶段无历史数据兼容问题