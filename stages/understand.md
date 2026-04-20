# Stage: Understand

职责：充分理解需求，产出无歧义任务描述，筛选本 Issue 相关知识。理解错了后面全错。

## Issue 分类处理

不同类型的 Issue 关注点不同，处理策略不同：

| Issue 类型 | 重点关注 | 轻量处理 |
|-----------|---------|---------|
| 新功能 | 用户场景、边界条件、与现有功能交互 | 可跳过影响分析 |
| Bug 修复 | 复现路径、根因、影响范围、回归风险 | 需要读取相关测试 |
| 重构 | 变更动机、不变量保证、迁移路径 | 可跳过用户场景 |
| 性能优化 | 基线数据、目标指标、测量方法 | 需要读取监控相关 |
| 依赖升级 | 升级原因、Breaking Changes、兼容性 | 需要读取依赖使用点 |

在 pipeline-state-{issue编号}.md Task section 开头标注 Issue 类型，后续阶段据此调整。
sssss
## 需求冲突处理

如果发现以下冲突，记入 Open Questions 🔴 阻塞，不自行裁决：

1. **Issue vs BRD/PRD** — Issue 描述与需求文档矛盾 → 以 BRD/PRD 为准，标注差异
2. **Issue vs 已有决策** — Issue 要求与已有 ADR 矛盾 → 记录矛盾点，不修改 ADR
3. **Issue 内部矛盾** — Issue 的不同描述段落互相矛盾 → 列出矛盾点
4. **隐含假设** — Issue 描述中未说明但必须成立的假设 → 显式列出

## Act

> 执行 `/pipeline-load understand` 进行知识加载

1. 写入 pipeline-state-{issue编号}.md：

**Task section**:
- Issue 类型: {新功能/Bug修复/重构/性能优化/依赖升级}
- 一句话描述：不了解项目的人也能看懂
- 用户场景: {谁在什么情况下做什么，期望什么结果}
- 为什么做：业务动机和用户价值
- 来源标注：每个需求点标注来自哪个文档的哪个 section

**Boundaries section**:
- 必须遵守：技术约束、合规要求、deadline
- 范围之内：本次要做的事（逐项列出）
- 范围之外：明确排除的事
- 检查：范围之内的每项不违反范围之外的约束

**Open Questions section**:
- 🔴 阻塞：不解决无法继续
- 🟡 不阻塞：需要关注但不阻塞
- 🟢 已解决

**References section**:
- 后续阶段需要的文件列表，每项写完整路径 + 一句话说明为什么需要
- 本 Issue 涉及的 knowledge 模块、相关 ADR、TRD 相关章节
- 禁止使用索引文件 — 直接写目标文件路径
- 按 downstream 消费顺序排列（design 先看的排前面）

2. 写 Issue 摘要（知识回流）：
   docs/issues/{issue名称}.md，按 docs/issues/README.md 模板

## Verify

自检（每项必须有具体证据）：
- [ ] Task 无歧义：把 Task section 给不了解项目的人看，他能理解。如果他问了"XX 是什么意思"，说明有歧义
- [ ] Boundaries 无重叠：范围之内的每项不违反范围之外的约束
- [ ] 需求有来源：每个需求点能追溯到 BRD/PRD/Issue 的具体 section
- [ ] Open Questions 完整：回顾所有读过的文档，无遗漏的模糊点
- [ ] References 可直达：下游阶段按 References 路径直接读取，无索引跳转
- [ ] References 完整：本 Issue 涉及的每个需要修改/参考的文件是否都在 References 中
- [ ] 冲突已暴露：Issue 与文档/已有决策的矛盾已记入 Open Questions

## 常见错误

- ❌ "实现登录功能" → ✅ "实现用户登录，支持手机号+验证码和邮箱+密码两种方式"
- ❌ References 里写 "见 decisions/README.md" → ✅ 直接写 "docs/issues/decisions/adr.md"
- ❌ 跳过 Issue 类型判断 → ✅ 先判断类型再确定关注重点
- ❌ 发现矛盾但不记录 → ✅ 记入 Open Questions，标注矛盾双方
