# ISSUE-全文检索

## 概述

实现文件和目录的全文检索功能，支持按文件名、内容、文件类型等维度进行快速搜索。

## 功能需求

### F-SEARCH-001 全文检索功能

| 需求项 | 内容 |
|--------|------|
| 需求名称 | 全文检索 |
| 需求描述 | 对文件名、文件内容、目录名称进行检索 |
| 模糊搜索 | 支持按关键词模糊匹配 |
| 筛选条件 | 按文件类型、大小范围、日期范围筛选 |
| 搜索结果 | 展示匹配的文件/目录，支持点击跳转 |
| 搜索历史 | 记录最近搜索关键词，支持快速重新搜索 |

### F-SEARCH-002 高级检索

| 需求项 | 内容 |
|--------|------|
| 内容检索 | 支持 txt、md 等文本文件内容搜索 |
| 文件类型筛选 | 按扩展名筛选（doc、xls、ppt、pdf、图片等） |
| 日期范围 | 按创建时间、修改时间筛选 |
| 大小范围 | 按文件大小区间筛选 |

## 技术方案

### 前端界面设计

#### 页面结构

```
┌─────────────────────────────────────────────────────────────┐
│  🔍 搜索文件或目录...                          [搜索] [筛选] │
├─────────────────────────────────────────────────────────────┤
│  筛选: [文件类型 ▼] [日期范围 ▼] [大小 ▼]                    │
├─────────────────────────────────────────────────────────────┤
│                                                            │
│  搜索结果 (共 15 项)                                       │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ 📄 项目计划.docx        /文档/项目       2026-03-15  │ │
│  │    大小: 2.3MB  类型: Word                            │ │
│  ├──────────────────────────────────────────────────────┤ │
│  │ 📄 会议纪要.docx        /文档/会议       2026-03-20  │ │
│  │    大小: 156KB  类型: Word                            │ │
│  ├──────────────────────────────────────────────────────┤ │
│  │ 📁 项目文档               /文档/项目                  │ │
│  │    包含 23 个子项                               │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
├─────────────────────────────────────────────────────────────┤
│  搜索历史: [项目] [计划] [2026] [会议]                       │
└─────────────────────────────────────────────────────────────┘
```

#### 搜索组件

| 组件 | 描述 | 事件 |
|------|------|------|
| `SearchBar` | 搜索输入框，支持回车搜索 | `search`, `clear` |
| `FilterPanel` | 筛选条件面板 | `filter-change` |
| `SearchResult` | 搜索结果列表 | `click-item`, `preview` |
| `SearchHistory` | 搜索历史记录 | `click-history` |
| `ResultHighlight` | 关键词高亮显示 | - |

#### 交互流程

1. **基础搜索**:
   ```
   输入关键词 → 自动搜索（防抖300ms） → 显示结果
   → 点击结果 → 跳转至目标位置
   ```

2. **筛选搜索**:
   ```
   选择筛选条件 → 应用筛选 → 刷新结果列表
   ```

3. **搜索历史**:
   ```
   点击历史关键词 → 自动填充搜索框 → 执行搜索
   ```

#### 异常提示

| 场景 | 提示信息 |
|------|----------|
| 无结果 | "未找到匹配的文件或目录，请尝试其他关键词" |
| 搜索中 | "搜索中..." |
| 搜索失败 | "搜索失败，请稍后重试" |
| 关键词为空 | "请输入搜索关键词" |

### 索引策略

#### 数据库索引设计

```sql
-- 全文检索索引（SQLite FTS5）
CREATE VIRTUAL TABLE file_fts USING fts5(
    name,
    content,
    content='t_file',
    content_rowid='id'
);

CREATE VIRTUAL TABLE directory_fts USING fts5(
    name,
    content='t_directory',
    content_rowid='id'
);

-- 同步触发器
CREATE TRIGGER file_ai AFTER INSERT ON t_file BEGIN
    INSERT INTO file_fts(rowid, name, content) VALUES (new.id, new.name, new.content);
END;

CREATE TRIGGER file_ad AFTER DELETE ON t_file BEGIN
    INSERT INTO file_fts(file_fts, rowid, name, content) VALUES('delete', old.id, old.name, old.content);
END;

CREATE TRIGGER file_au AFTER UPDATE ON t_file BEGIN
    INSERT INTO file_fts(file_fts, rowid, name, content) VALUES('delete', old.id, old.name, old.content);
    INSERT INTO file_fts(rowid, name, content) VALUES (new.id, new.name, new.content);
END;
```

#### 搜索策略

1. **文件名搜索**: 使用 FTS5 的 name 列
2. **内容搜索**: 使用 FTS5 的 content 列（仅文本文件）
3. **目录搜索**: 单独使用 directory_fts

### 接口设计

| 接口 | 方法 | 路径 | 功能 |
|------|------|------|------|
| 搜索 | GET | /api/search | 搜索文件和目录 |
| 搜索建议 | GET | /api/search/suggest | 搜索建议（自动补全） |
| 搜索历史 | GET | /api/search/history | 获取搜索历史 |
| 清空历史 | DELETE | /api/search/history | 清空搜索历史 |

#### 请求参数

```
GET /api/search?q=关键词&type=file|directory|all&fileType=doc,docx&startDate=2026-01-01&endDate=2026-12-31&minSize=0&maxSize=10485760&page=1&size=20
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| q | string | 是 | 搜索关键词 |
| type | string | 否 | 搜索类型: file/directory/all，默认 all |
| fileType | string | 否 | 文件类型筛选，逗号分隔 |
| startDate | string | 否 | 开始日期 |
| endDate | string | 否 | 结束日期 |
| minSize | long | 否 | 最小文件大小（字节） |
| maxSize | long | 否 | 最大文件大小（字节） |
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页数量，默认 20 |

### 业务规则

1. 搜索关键词最少 1 个字符
2. 搜索结果按相关度排序
3. 文本文件支持内容搜索
4. 搜索历史保留最近 10 条
5. 支持中英文搜索
6. 搜索结果分页展示
7. 搜索防抖 300ms

### 依赖技术

- SQLite FTS5 - 全文搜索引擎
- Vue3 - 前端框架
- Element Plus - UI 组件库

## 验收条件

1. **基础搜索** → 输入关键词显示匹配结果
2. **文件名搜索** → 模糊匹配文件名
3. **内容搜索** → txt、md 文件内容可搜索
4. **类型筛选** → 按文件类型筛选结果
5. **日期筛选** → 按创建/修改时间筛选
6. **大小筛选** → 按文件大小区间筛选
7. **搜索历史** → 显示最近搜索关键词
8. **关键词高亮** → 搜索结果中高亮关键词
9. **点击跳转** → 点击结果跳转到对应目录
10. **无结果提示** → 无结果时显示友好提示

## 依赖

- 目录管理模块
- 文件管理模块
- 数据库模块

## 状态

- [ ] 待开发
- [ ] 开发中
- [ ] 待测试
- [x] 完成

## 实现摘要

### 待实现