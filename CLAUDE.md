# Issue Development Pipeline

## 项目概况

Agentic Coding Platform — 通用 agent 任务执行平台，封装 Claude Code CLI 提供托管执行环境。
当前阶段: MVP (v1.0) | 设计原则: Modular Monolith | Reuse First | Fast Validation

## 项目结构

agent-infra/
├── cmd/            # 程序入口
├── internal/
│ ├── api/          # HTTP 层: handler, middleware, router
│ ├── service/      # 业务逻辑核心，事务边界
│ ├── repository/   # 数据访问层
│ ├── model/        # GORM 模型定义
│ ├── scheduler/    # 调度引擎
│ ├── executor/     # 执行引擎: Job 生命周期, Pod 管理
│ ├── config/       # 配置加载
│ ├── migration/    # 数据库迁移
│ ├── monitoring/   # 监控采集
│ └── seed/         # 种子数据
├── pkg/            # 公共工具
├── web/            # 前端: React + Ant Design
└── deploy/         # K8s manifests, Dockerfiles

## 架构概览

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Frontend   │────▶│    API       │────▶│   Service    │
│  React+AntD  │     │  Gin+Router │     │  业务逻辑核心  │
└─────────────┘     └─────────────┘     └──────┬──────┘
                                               │
                                    ┌──────────┼──────────┐
                                    ▼          ▼          ▼
                              ┌──────────┐ ┌──────┐ ┌──────────┐
                              │Repository│ │Redis │ │Scheduler │
                              │ GORM+DB  │ │Cache │ │ 调度引擎  │
                              └──────────┘ └──────┘ └────┬─────┘
                                                        │
                                                  ┌─────▼─────┐
                                                  │ Executor   │
                                                  │ K8s+Pod   │
                                                  └───────────┘
```

## 核心概念

| 概念 | 含义 |
|------|------|
| Task | 用户提交的执行任务，包含代码仓库、分支、环境变量 |
| Job | Task 的一次执行实例，有状态机（pending→running→succeeded/failed） |
| Pod | Job 的运行容器，K8s 管理，有资源限制和超时 |
| Provider | Agent 运行时配置，定义 MCP 工具、环境变量、模型选择 |
| Capability | Agent 能力注册，定义工具集合和权限边界 |
| Intervention | 人工干预点，Job 在特定阶段暂停等待人工审批 |
| Checkpoint | 执行快照，Job 可从任意 checkpoint 恢复执行 |

## 分层约束

```
Handler → Service → Repository → Model
```

- Handler: 参数校验 + 响应格式化，不含业务逻辑
- Service: 业务逻辑核心，事务边界，可调用多个 Repository
- Repository: 数据访问层，封装 SQL，不含业务逻辑
- Model: GORM 模型定义，不含方法

禁止路径: Handler → Repository, Repository → Service, Scheduler/Executor → Database

## 启动

1. `git pull origin main`
2. 创建 worktree：`git worktree add -b feature/issue-{N} .claude/worktrees/issue-{N} main`
3. 复制 stages/pipeline-state-template.md → .claude/pipeline-state.md，填入 Task + 技术栈 + 架构约束
4. 复制 stages/pipeline-log-template.md → .claude/pipeline-log.md
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
   完成后严格遵循 stage 文件执行。
   ```
   **主 agent 职责**：review 完成后，主 agent 将评审结论写入 pipeline-state.md Current Stage > 评审 section（状态、轮次、反馈）
7. review 不通过 → 反馈写入 state → 修复 → 最多重试 2 次 → 人工介入
8. 子 agent 失败 → 重试 1 次 → 仍失败 → 人工介入
9. Boundaries 冲突 → 停止
10. 写日志（按 stages/logging-guide.md）
11. **SR/QR 偏差处理**：如果 SR 偏差 ≥ 3 项，或 QR 偏差 ≥ 2 项，暂停并升级人工介入。否则记录偏差，继续下一阶段
12. 更新 state → 下一阶段

## 断点续跑

session 中断后重新启动：
1. 读 pipeline-state.md 的 Current Stage
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
