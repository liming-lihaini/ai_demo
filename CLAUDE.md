# Issue Development Pipeline

## 项目概况

文件管理系统 — 桌面端本地文件管理工具，提供文件目录管理、文件操作、文件预览、在线Markdown编写功能。
当前阶段: MVP (v1.0) | 设计原则: 前后端不分离 | 离线优先 | 快速验证

## 项目结构

file-manager/                           # 主项目目录
├── pom.xml                            # Maven 构建配置
├── src/main/
│   ├── java/com/filemanager/
│   │   ├── Application.java           # 启动类 (main方法)
│   │   ├── config/                   # 配置层
│   │   │   └── DatabaseInitializer.java # 数据库初始化
│   │   ├── controller/               # HTTP接口层 (空)
│   │   ├── model/                   # 数据模型层
│   │   │   ├── Directory.java        # 目录实体
│   │   │   ├── FileInfo.java         # 文件实体
│   │   │   └── User.java            # 用户实体
│   │   ├── repository/               # 数据访问层 (空)
│   │   ├── service/                 # 业务逻辑层 (空)
│   │   └── util/                    # 工具类 (空)
│   ├── resources/
│   │   ├── application.yml           # 应用配置
│   │   ├── mapper/                  # MyBatis映射 (空)
│   │   └── static/                 # 静态资源 (空)
│   └── webapp/
│       ├── src/
│       │   ├── main.js              # 前端入口
│       │   ├── router/              # 路由配置
│       │   ├── api/                 # 前端API调用
│       │   ├── components/           # Vue组件
│       │   ├── views/               # 页面视图
│       │   └── assets/              # 静态资源
│       ├── index.html
│       ├── vite.config.js           # Vite配置
│       └── package.json             # 前端依赖
├── target/                           # 编译输出目录
└── db/                              # SQLite数据库文件 (运行时生成)

## 架构概览

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Frontend   │────▶│    API       │────▶│   Service    │
│  Vue3+EltPl  │     │  SpringBoot  │     │  业务逻辑核心  │
└─────────────┘     └─────────────┘     └──────┬──────┘
                                               │
                                    ┌──────────┼──────────┐
                                    ▼          ▼          ▼
                              ┌──────────┐ ┌──────┐ ┌──────────┐
                              │SQLite+FS │ │Cache │ │Scheduler │
                              │  本地存储 │ │本地缓存 │ │ 定时任务  │
                              └──────────┘ └──────┘ └────┬─────┘
                                                        │
                                                  ┌─────▼─────┐
                                                  │  打包exe  │
                                                  │ Launch4j  │
                                                  └───────────┘
```

## 核心概念

| 概念 | 含义 |
|------|------|
| 目录 | 用于组织文件的层级结构，支持嵌套创建/移动/删除 |
| 文件 | 存储在目录中的具体文档，支持上传/下载/预览 |
| 回收站 | 删除文件临时存储，30天后自动清除 |
| MD文件 | Markdown格式文本，支持实时预览/导出 |
| 预览转换 | Office文档转换为PDF后预览 |

## 架构约束

### 分层约束

```
Controller → Service → Repository → Model
```

| 层级 | 职责 | 禁止操作 |
|------|------|----------|
| Controller | 参数校验 + 响应格式化 + 参数组装 | 禁止执行业务逻辑 |
| Service | 业务逻辑核心，事务边界，可调用多个Repository | 禁止直接访问数据库 |
| Repository | 数据访问层，封装SQL查询 | 禁止执行业务逻辑 |
| Model | 数据模型定义，仅getter/setter | 禁止业务逻辑 |

**禁止路径**：
- ❌ Controller → Repository（应通过Service）
- ❌ Repository → Service（直接查库返回实体）
- ❌ Service → 直接使用JDBC/MyBatis
- ✅ Controller → Service → Repository → Model

### 后端约束

| 约束项 | 规则 |
|---------|------|
| 事务边界 | Service层方法开启事务，Repository不开启 |
| 参数校验 | Controller层使用@Valid注解校验 |
| 异常处理 | Service层抛异常，Controller层统一捕获 |
| 响应格式 | 统一 {code, message, data} JSON格式 |
| 分页查询 | Repository支持但不含业务逻辑 |
| 软删除 | 使用@Query手动写SQL，Service层处理 |
| 日志记录 | 操作日志在Service层记录 |
| 定时任务 | @Scheduled在Service层，不在Repository/Controller |

### 前端约束

| 约束项 | 规则 |
|---------|------|
| 目录结构 | views/页面、components/组件、api/接口、router/路由 |
| 组件通信 | props down, events up，禁止直接修改props |
| 状态管理 | 使用store保存全局状态，组件内状态用ref/reactive |
| API调用 | 统一通过api/目录封装，禁止直接fetch/axios |
| 样式隔离 | scoped styles + BEM命名 |
| 路由懒加载 | 路由使用() => import()实现代码分割 |
| 环境变量 | .env文件区分dev/prod |
| 表单验证 | 使用el-form + async-validator |

### 数据库约束

| 约束项 | 规则 |
|---------|------|
| 实体命名 | 驼峰命名，数据库下划线映射 |
| 软删除 | 使用deleted字段，Service层过滤 |
| 索引 | 根据查询需求在Repository定义 |
| 关联查询 | 优先用id关联，避免join |

### 命名约束

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| Java类 | UpperCamelCase | DirectoryService |
| Java方法 | lowerCamelCase | getChildDirectories() |
| Java变量 | lowerCamelCase | currentParentId |
| 数据库表 | 复数+下划线 | directories, files |
| Vue组件 | PascalCase | DirectoryManager.vue |
| Vue方法 | lowerCamelCase | handleNodeClick |
| API路径 | RESTful风格 | /api/directory/create |

## 启动

1. `git pull origin main`
2. 创建 worktree：`git worktree add -b feature/issue-{N} .claude/worktrees/issue-{N} main`
3. 复制 stages/pipeline-state-template.md → .claude/pipeline-state-{issue编号}.md，  在文件中填入 Task + 技术栈 + 架构约束
4. 复制 stages/pipeline-log-template.md → .claude/pipeline-log-{issue编号}.md
5. 记录 Pipeline 版本到 state


## 阶段

```
understand → design → [review] → plan → [review] → implement → [review] → deliver
```

## 执行循环

对每个阶段：

1. 读 pipeline-state.md 确认当前阶段
2. 准备子 agent prompt：
   - 如当前阶段不是 understand → 读 state References section，提取完整文件路径列表
   - understand 阶段不需要知识文件列表
3. 创建子 agent：
   ```
   [{阶段名} agent] {Issue 标题}

   运行 /pipeline-load {阶段名}。
   知识文件: {上一步提取的文件路径，逗号分隔}
   完成后严格遵循 stage 文件执行。
   ```
4. 等子 agent 完成
5. **Self-review（必须执行，不可跳过）**：
   按 stages/logging-guide.md 的双轨评估执行：
   - 轨道 A: SR-0 ~ SR-8（流程合规）
   - 轨道 B: QR-0 ~ QR-3（产出质量）
   每项写"合规"或"偏差: {具体描述}"。
   全部 SR + QR 结果写入日志"验证"段。
   **如果日志中缺少 SR 或 QR section，视为本阶段未完成。**
6. 如需 review → 创建 review 子 agent：
   ```
   [review agent] {被评审阶段} - {Issue 标题}

   运行 /pipeline-load review。
   完成后严格遵循 stage 文件执行
   ```
   **主 agent 职责**：review 完成后，主 agent 将评审结论写入 pipeline-state-{issue名称}.md Current Stage > 评审 section（状态、轮次、反馈）
7. review 不通过 → 反馈写入 state → 修复 → 最多重试 2 次 → 人工介入
8. 子 agent 失败 → 重试 1 次 → 仍失败 → 人工介入
9. Boundaries 冲突 → 停止
10. 写日志（按 stages/logging-guide.md）
11. **SR/QR 偏差处理**：如果 SR 偏差 ≥ 3 项，或 QR 偏差 ≥ 2 项，暂停并升级人工介入。否则记录偏差，继续下一阶段
12. 更新 state → 下一阶段

## 断点续跑

session 中断后重新启动：
1. 读 pipeline-state-{issue编号}.md 的 Current Stage
2. 从该阶段重新执行（已完成阶段产出保留）
3. 日志标注"恢复执行"

## 异常处理

| 异常 | 动作 | 恢复方式 |
|------|------|---------|
| 子 agent 失败 | 重试 1 次 | 仍失败 → 人工介入 |
| Review 不通过 | 反馈→修复→重审 | 最多 2 轮 → 人工介入 |
| Boundaries 冲突 | 停止 | 人工调整 Boundaries |
| State 未更新 | 主 agent 修正 | — |
| Session 中断 | 见断点续跑 | 从 Current Stage 恢复 |

## 完成后

按 stages/self-evolution.md 执行退化分析，追加 Iteration 记录。
