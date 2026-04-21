---
name: pipeline-load
description: 加载指定阶段的pipeline知识，按顺序加载阶段指令、项目上下文和Issue状态
version: 1.0
author: Local Skill
---

# Pipeline 知识加载

## 输入

$ARGUMENTS = 阶段名 (如 understand, design, plan, implement, deliver)

## 加载原则

1. 必要性优先 — 不读无法工作的内容排前面
2. 稳定性其次 — 越稳定的内容 prompt cache 命中率越高
3. 结尾放核心决策输入 — 利用 lost-in-middle 效应

## 第一步：固定加载

1. `docs/knowledge/stages/{$ARGUMENTS}.md` — 本阶段执行指令（必须加载）
2. `CLAUDE.md` — 项目上下文和架构约束（必须加载）

## 第二步：规范查询

### L1 项目规范（必须加载）
**design / plan / implement 阶段**：
必须加载 `docs/knowledge/rulers/` 下的约束文件，作为设计/实现的验收条件：

| 阶段 | 必须加载文件 | 用途 |
|------|-------------|------|
| design | `docs/knowledge/rulers/Code.md` | 架构约束、API 设计约束 |
| design | `docs/knowledge/rulers/SEC.md` | 安全红线、编码安全基线 |
| plan | `docs/knowledge/rulers/Code.md` | 实现约束、代码规范 |
| plan | `docs/knowledge/rulers/SEC.md` | 安全实现要求 |
| implement | `docs/knowledge/rulers/Code.md` | 开发约束、自检清单 |
| implement | `docs/knowledge/rulers/SEC.md` | 代码安全验证 |

### L2 领域知识（可选）
按 Issue 上下文查询业务领域概念：
- 涉及的核心业务概念定义
- 涉及的状态机和生命周期
- 涉及的业务规则和约束

> **MVP 降级**: 如果 L2/L3 知识库工具不可用，跳过此步

## 第三步：项目知识

### understand 阶段
- `docs/issues/issue-{N}-*.md` — Issue 描述文件
- `docs/current/BRD.md` — 业务需求（如有）
- `docs/current/PRD.md` — 产品需求（如有）
- `docs/current/decisions/` 下已有 ADR

### design / plan / implement / deliver 阶段
- 从 pipeline-state-{issue编号}.md 的 References section 读取知识文件列表
- 按列表顺序加载，不做额外扩展

## 第四步：Issue 状态（最后读取）

- `.claude/pipeline-state-{issue编号}.md` — 完整读取

> 放最后：Task + Boundaries + Decisions + Current Stage 是最关键的决策输入

## 完成确认

1. 复述 pipeline-state-{issue编号}.md 的 Task、Boundaries、上一阶段产出（Handoff 检查）
2. 确认已加载的知识足以执行本阶段任务
3. 如有缺失，记入 Open Questions 并说明原因