# Pipeline State — Template

> 子 agent 写入，主 agent 消费（提取 References 构造后续 prompt）。

## Project Context

项目: {名称} | 阶段: {MVP/v1.0/...} | Pipeline 版本: v{N}
技术栈: {Go/React/DB 等}
架构: Handler → Service → Repository → Model
设计原则: {Modular Monolith | Reuse First | Fast Validation}

## Task

Issue: #{issue_number}
Title: {issue_title}
Issue 类型: {新功能/Bug修复/重构/性能优化/依赖升级}
一句话描述: {不了解项目的人也能看懂}
用户场景: {谁在什么情况下做什么，期望什么结果}
为什么做: {业务动机}
来源: {每个需求点的文档引用}

## Decisions

### {决策标题}
选择: {选了什么} | 原因: {引用项目实际}
否决: {排除了什么} — {为什么}
影响: {对后续实现的影响}
阶段: {哪个阶段}

## Boundaries

必须遵守:
- {硬约束}

范围之内:
- {本次要做的}

范围之外:
- {明确排除的}

## Open Questions

🔴 阻塞: {如有}
🟡 不阻塞: {如有}
🟢 已解决: {如有}

## Current Stage

阶段: {阶段名}
指令: stages/current/{阶段名}.md
关注: {section 列表} — {为什么关注}

### 产出
状态: {⏳ | ✅}
文件:
  - {完整路径}: {一句话说明}

### 自检
- [ ] {标准}: {验证方法} — {结果}

### 评审
状态: {⬚ | ✅ | ❌}
轮次: {N}
反馈: {阻塞/建议}

## References

> 主 agent 在 understand 完成后提取此列表，构造后续阶段的 sub-agent prompt。
> 后续阶段不通过此 section 读取文件。

{每项写完整路径，禁止使用索引文件}
- {完整路径}: {一句话说明}

## 知识回流

| 阶段 | 回流类型 | 文件路径 | 状态 |
|------|---------|---------|------|
| understand | Issue 摘要 | docs/current/issues/issue-{N}-{title}.md | ⏳/✅ |
| design | ADR | docs/current/decisions/adr-{topic}.md | ⏳/✅ |
| implement | Knowledge 更新 | docs/knowledge/{module}.md | ⏳/✅/N/A |
| implement | Pipeline 文件修改 | stages/*.md, commands/*.md, CLAUDE.md | ⏳/✅/N/A |

> Pipeline 文件修改通过 self-evolution.md 处理，不通过 knowledge 回流。

## Deliver

PR: {链接或 N/A}
创建时间: {时间}
