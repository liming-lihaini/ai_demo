# Pipeline 知识加载

阶段: $ARGUMENTS

## 加载原则

1. 必要性优先 — 不读无法工作的内容排前面
2. 稳定性其次 — 越稳定的内容 prompt cache 命中率越高
3. 结尾放核心决策输入 — 利用 lost-in-middle 效应

## 第一步：固定加载

1. .claude/stages/$ARGUMENTS.md — 本阶段执行指令
2. .claude/stages/current/ — 确认指向当前版本

## 第二步：规范查询（稳定性高）

### L3 企业规范

按当前任务场景查询企业知识库中的规范条目。以下为参考方向，根据实际任务扩展：

- 写代码 → 编码规范、Git commit 规范、错误处理规范
- 写接口 → API 设计规范、认证鉴权规范、错误码规范
- 操作数据 → 数据库命名规范、索引规范、迁移规范
- 安全相关 → 安全编码规范、数据脱敏规范、权限规范
- 发布部署 → 分支规范、CI/CD 规范、发布流程

### L2 领域知识

按 Issue 上下文查询业务领域概念。以下为参考方向，根据实际任务扩展：

- 涉及的核心业务概念定义
- 涉及的状态机和生命周期
- 涉及的业务规则和约束

只查与当前任务直接相关的条目，不预加载。

> **MVP 降级**: 如果 L2/L3 知识库工具不可用，跳过此步，在完成确认中记入
> "L2/L3 不可用，依赖项目文档和现有代码模式"。

## 第三步：项目知识

### understand
5. Issue 描述文件
6. docs/current/BRD.md
7. docs/current/PRD.md
8. docs/current/TRD.md（仅相关章节）
9. docs/knowledge/{modules}.md（按 Issue 涉及的模块按需读取）
10. docs/current/decisions/ 目录下已有 ADR（仅与当前 Issue 相关的）

### design / plan / implement / review / deliver
5. prompt 中的知识文件列表（按路径直接读取，不经过索引）

## 第四步：Issue 状态（最后读取）

6. .claude/pipeline-state.md — 完整读取

> 放最后：Task + Boundaries + Decisions + Current Stage 是最关键的决策输入，
> 结尾读取确保注意力最大化。

## 完成确认

1. 复述 pipeline-state.md 的 Task、Boundaries、上一阶段产出（Handoff 检查）
2. 确认已加载的知识足以执行本阶段任务
3. 如有缺失，记入 pipeline-state.md Open Questions 并说明原因
