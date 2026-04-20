# ISSUE-回收站

## 概述

实现回收站功能，包括查看、恢复、清空、自动清理已删除的文件和目录。

## 功能需求

### F-TRASH-001 回收站功能

| 需求项 | 内容 |
|--------|------|
| 需求名称 | 回收站管理 |
| 需求描述 | 管理回收站中的文件和目录 |
| 显示 | 展示回收站中的目录和文件列表 |
| 恢复 | 支持单个/批量恢复至原目录 |
| 自动清理 | 30天后自动永久删除 |
| 手动清空 | 支持一键清空（清空后不可恢复） |

## 技术方案

### 前端界面设计

#### 页面结构

```
┌────────────────────────────────────────────────────────────┐
│  回收站                                     [清空] [刷新]  │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  已删除文件 (共 23 项, 1.2GB)                           │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ ☑ │ 名称        │ 原位置        │ 删除时间   │ 剩余天数 │ │
│  ├────────────────────────────────────────────────────────┤ │
│  │ ☑ │ 文档.docx   │ /文档/项目   │ 2026-03-15 │  15天  │ │
│  │ ☐ │ 照片.jpg    │ /图片/旅行   │ 2026-03-20 │  10天  │ │
│  │ ☑ │ 笔记.md     │ /笔记/日志   │ 2026-04-01 │   5天  │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  已选择 2 项                               [恢复] [彻底删除] │
│                                                            │
├────────────────────────────────────────────────────────────┤
│  提示: 超过30天的文件将被自动永久删除                        │
└────────────────────────────────────────────────────────────┘

┌──────────────────────────────────┐
│       确认清空回收站             │
│  确定清空回收站？                │
│  清空后文件将被永久删除            │
│  无法恢复                         │
│                                │
│     [取消]  [确认清空]          │
└──────────────────────────────────┘

┌──────────────────────────────────┐
│       恢复文件                   │
│  恢复文件: 文档.docx             │
│  恢复至: /文档/项目              │
│                                │
│     [取消]  [确认恢复]          │
└──────────────────────────────────┘

┌──────────────────────────────────┐
│       提示                       │
│  原目录 "/文档/项目" 已不存在     │
│  请选择恢复位置:                  │
│                                │
│  [选择目录...]                   │
│                                │
│     [取消]  [确认恢复]          │
└──────────────────────────────────┘
```

#### 关键组件

| 组件 | 描述 | 事件 |
|------|------|------|
| `TrashList` | 回收站列表组件，显示删除项 | `select`, `restore`, `delete` |
| `TrashToolbar` | 工具栏: 全选/清空/刷新 | `empty`, `refresh` |
| `RestoreDialog` | 恢复对话框 | `confirm`, `cancel` |
| `ConfirmDialog` | 确认清空弹窗 | `confirm`, `cancel` |
| `ExpireTimer` | 过期倒计时显示 | `tick` |
| `BatchActions` | 批量恢复/彻底删除 | `batch` |

#### 交互流程

1. **查看回收站**:
   ```
   点击"回收站"入口 → 加载回收站列表 → 显示删除项
   → 显示原位置和剩余天数
   ```

2. **恢复文件**:
   ```
   勾选文件 → 点击"恢复" → 确认恢复对话框
   → 调用恢复接口 → 刷新列表 → 成功提示
   ```

3. **批量恢复**:
   ```
   勾选多个文件 → 批量恢复 → 逐个恢复
   → 显示恢复结果
   ```

4. **清空回收站**:
   ```
   点击"清空" → 二次确认弹窗 → 确认后
   → 删除全部 → 刷新列表 → 清空成功
   ```

#### 异常提示

| 场景 | 提示信息 |
|------|----------|
| 清空确认 | "清空后文件将被永久删除，无法恢复，确定要清空吗？" |
| 恢复失败 | "恢复失败，原目录已被删除，请选择其他位置" |
| 批量恢复 | "部分文件恢复失败，已恢复 X 个，失败 Y 个" |
| 原目录不存在 | "原目录已不存在，请选择恢复位置" |

### 回收站机制

使用 `t_directory` 和 `t_file` 表的 `deleted` 和 `delete_at` 字段实现软删除：

```sql
-- 软删除标记
ALTER TABLE t_directory ADD COLUMN deleted TINYINT DEFAULT 0;
ALTER TABLE t_directory ADD COLUMN delete_at DATETIME;

ALTER TABLE t_file ADD COLUMN deleted TINYINT DEFAULT 0;
ALTER TABLE t_file ADD COLUMN delete_at DATETIME;
```

### 自动清理流程

```java
// 每日定时任务执行
@Scheduled(cron = "0 0 2 * * ?")  // 凌晨2点执行
public void autoCleanExpiredItems() {
    // 查询30天前的已删除记录
    List<Directory> expiredDirs = directoryRepository.findByDeletedAndDeleteAtBefore(
        1, LocalDateTime.now().minusDays(30));
    List<File> expiredFiles = fileRepository.findByDeletedAndDeleteAtBefore(
        1, LocalDateTime.now().minusDays(30));

    // 物理删除文件和数据库记录
    for (Directory dir : expiredDirs) {
        storageService.deleteDirectory(dir.getPath());
        directoryRepository.delete(dir);
    }
    for (File file : expiredFiles) {
        storageService.deleteFile(file.getStoragePath());
        fileRepository.delete(file);
    }
}
```

### 接口设计

| 接口 | 方法 | 路径 | 功能 |
|------|------|------|------|
| 回收站列表 | GET | /api/trash | 获取回收站列表 |
| 恢复 | POST | /api/trash/{id}/restore | 恢复文件/目录 |
| 永久删除 | DELETE | /api/trash/{id} | 永久删除 |
| 清空回收站 | DELETE | /api/trash/clear | 清空全部 |

### 业务规则

1. 保留30天后自动永久删除
2. 恢复时检查原目录是否存在
3. 清空操作需二次确认
4. 恢复后 `deleted=0`，`delete_at=null`
5. 物理删除后同时删除存储文件

### 依赖技术

- Spring Scheduled - 定时任务
- 数据库软删除 - 逻辑标记

## 验收条件

1. **查看回收站** → 显示已删除的文件/目录列表
2. **恢复单个** → 文件/目录回到原目录
3. **批量恢复** → 多个恢复到原目录
4. **手动清空** → 确认后清空全部 → 不可恢复
5. **自动清理** → 30天后自动删除过期项
6. **原目录不存在** → 恢复提示"原目录已不存在"
7. **清空确认** → 二次确认弹窗

## 依赖

- 目录管理模块
- 文件管理模块
- 定时任务模块

## 状态

- [x] 待开发
- [x] 开发中
- [x] 待测试
- [x] 完成
- [x] 已关闭 (2026/04/20) via PR #7

## 实现摘要

### 完成的实现
1. FileInfo 和 Directory 模型添加 `deleteAt` 字段
2. FileService 删除方法设置 `deleteAt` 时间戳
3. DirectoryService 删除方法设置 `deleteAt` 时间戳
4. TrashService 实现回收站核心业务逻辑：
   - getTrashList: 获取回收站列表
   - restore: 恢复文件/目录
   - permanentDelete: 永久删除
   - clearTrash: 清空回收站
   - cleanExpiredItems: 定时清理（每天凌晨2点，30天过期）
5. TrashController 实现 API 接口：/api/trash

### 技术决策
- 采用软删除方案，使用 `deleted` + `deleteAt` 字段
- 定时任务使用 `@Scheduled(cron = "0 0 2 * * ?")` 每天凌晨2点执行
- API 路径使用 `/api/trash` 前缀，符合 RESTful 风格
- 遵循 CLAUDE.md 架构约束：Controller → Service → Repository → Model

### 与计划的偏差
- 无重大偏差