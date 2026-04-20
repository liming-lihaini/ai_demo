# ADR-6-4: 搜索 API 设计

## 背景

Issue #6 需要提供搜索接口，需要设计 RESTful API。

## 方案

### 方案 A: 单一搜索接口

GET /api/search?q=关键词&type=file|directory|all

- 优点: 接口简洁
- 缺点: 筛选参数需全部放在 query string

### 方案 B: 多个独立接口

- GET /api/search: 搜索
- GET /api/search/suggest: 搜索建议
- GET /api/search/history: 搜索历史（可选）

- 优点: 职责分离、接口语义清晰
- 缺点: 需维护多个接口

## 选择

选择: 方案 B（多个独立接口）

原因: 搜索建议和搜索历史是独立功能，分离接口便于维护和扩展

## 接口设计

| 接口 | 方法 | 路径 | 功能 |
|------|------|------|------|
| 搜索 | GET | /api/search | 搜索文件和目录 |
| 搜索建议 | GET | /api/search/suggest | 自动补全建议 |
| 搜索历史 | GET | /api/search/history | 获取搜索历史 |
| 清空历史 | DELETE | /api/search/history | 清空搜索历史 |

## 状态

- [x] 已确定
