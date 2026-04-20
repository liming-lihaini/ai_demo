# Pipeline Log — Issue {issue编号} {标题}

> 所有阶段日志统一写入此文件。
> 命名格式: pipeline-log-{issue编号}.md
> 阶段分隔: 两个空行

---

## 元信息

- Issue: #{issue编号} {标题}
- 状态: {进行中/已完成}
- Pipeline 版本: v1
- 创建时间: {日期}

---

## understand | {开始} - {结束} | {✅/进行中}

### 输入

- State 关注: Task, Boundaries
- 阶段知识: `docs/knowledge/stages/understand.md`
- 项目知识:
  - `docs/issues/issue-{N}-{标题}.md`: Issue 描述
  - `docs/current/BRD.md`: 业务需求
  - `docs/current/PRD.md`: 产品需求
  - `docs/current/decisions/` 下已有 ADR

### 过程

- 步骤: {动作} → {结果}
- 推理: {观察} → {结论}
- 困难: {问题} → {解决}

### 输出

- 产出: {文件路径} - {内容摘要}
- State 变更:
  - Task: {已填充}
  - Boundaries: {已明确}

### 验证

#### 轨道 A：流程合规
SR-0 流程遵循 | {合规/偏差}: {详情}
SR-1 加载顺序 | {合规/偏差}: {详情}
SR-2 加载完整性 | {合规/偏差}: {详情}
SR-3 Handoff 检查 | N/A: 本阶段为起始
SR-4 Act 步骤遵循 | {合规/偏差}: {详情}
SR-5 Verify 执行 | {合规/偏差}: {详情}
SR-6 State 更新 | {合规/偏差}: {详情}
SR-7 越界 | {合规/偏差}: {详情}
SR-8 知识回流 | {合规/偏差}: {详情}

#### 轨道 B：产出质量
QR-0 决策一致性 | {合规/偏差}: {详情}
QR-1 边界合规 | {合规/偏差}: {详情}
QR-2 验收可验证 | {合规/偏差}: {详情}
QR-3 下游可用性 | {合规/偏差}: {详情}

### 观察

- 发现: {现象} → {原因}
- 异常: {异常} → {处理}


---

## design | {开始} - {结束} | {✅/进行中}

### 输入

- State 关注: Task, Boundaries, Open Questions
- 阶段知识: `docs/knowledge/stages/design.md`
- 规则知识:
  - `docs/knowledge/rulers/Code.md`: 编码规范约束
  - `docs/knowledge/rulers/SEC.md`: 安全合规约束

### Handoff

- 阶段: understand
- 核心产出: {Issue 摘要、已明确的 Boundaries}
- 信息充分性: {充分/缺失项}

### 过程

- 步骤: {动作} → {结果}
- 推理: {观察} → {结论}
- 备选:
  - 方案 A: {描述} - 优点/缺点
  - 方案 B: {描述} - 优点/缺点
  - 选择: {方案X} | 原因: {为什么}

### 输出

- 产出:
  - `docs/current/decisions/adr-{N}-{主题}.md`: ADR 文档
- State 变更:
  - Decisions: {已确定的技术决策}
  - Open Questions: {已解决/仍存在}

### 验证

#### 轨道 A：流程合规
SR-0 ~ SR-8 | {合规/偏差}: {详情}

#### 轨道 B：产出质量
QR-0 ~ QR-3 | {合规/偏差}: {详情}

#### 规则验证
| 约束文件 | 规则ID | 验证结果 |
|---------|--------|---------|
| Code.md | | |
| SEC.md | | |

### 观察

- 发现: {现象} → {原因}
- 异常: {异常} → {处理}


---

## plan | {开始} - {结束} | {✅/进行中}

### 输入

- State 关注: Task, Boundaries, Decisions
- 阶段知识: `docs/knowledge/stages/plan.md`
- 规则知识:
  - `docs/knowledge/rulers/Code.md`
  - `docs/knowledge/rulers/SEC.md`
- References: {从 state References 读取的文件列表}

### Handoff

- 阶段: design
- 核心产出: {ADR 决策文档}
- 关键决策: {列出已做决策}
- 信息充分性: {充分/缺失项}

### 过程

- 步骤: {动作} → {结果}
- 推理: {观察} → {结论}

### 输出

- 产出:
  - `docs/plans/{日期}-{功能}实现计划.md`: 实现计划
- State 变更:
  - Current Stage: plan
  - Task 列表: {拆分的 Task}

### 验证

#### 轨道 A：流程合规
SR-0 ~ SR-8 | {合规/偏差}: {详情}

#### 轨道 B：产出质量
QR-0 ~ QR-3 | {合规/偏差}: {详情}

#### 规则验证
| 约束文件 | 规则ID | 验证结果 |
|---------|--------|---------|
| Code.md | | |
| SEC.md | | |

### 观察

- 发现: {现象} → {原因}
- 异常: {异常} → {处理}


---

## implement | {开始} - {结束} | {✅/进行中}

### 输入

- State 关注: Task, Boundaries, 实现产出
- 阶段知识: `docs/knowledge/stages/implement.md`
- 规则知识:
  - `docs/knowledge/rulers/Code.md`
  - `docs/knowledge/rulers/SEC.md`
- References: {从 state References 读取的文件列表}

### Handoff

- 阶段: plan
- 核心产出: {实现计划、Task 列表}
- 关键决策: {技术实现方案}
- 信息充分性: {充分/缺失项}

### 补读

- {文件路径}: {原因}
（如无补读写"补读: 无"）

### 过程

- Task 1: {任务} → {结果}
- Task 2: {任务} → {结果}
- ...

### 输出

- 产出:
  - {文件路径}: {修改内容}
- State 变更:
  - Current Stage: implement
  - 产出文件列表: {已创建/修改的文件}

### 验证

#### 轨道 A：流程合规
SR-0 ~ SR-8 | {合规/偏差}: {详情}

#### 轨道 B：产出质量
QR-0 ~ QR-3 | {合规/偏差}: {详情}

#### 规则验证
| 约束文件 | 规则ID | 验证结果 |
|---------|--------|---------|
| Code.md | JAVA-MUST-0001 | {合规/偏差} |
| Code.md | JAVA-MUST-0002 | {合规/偏差} |
| SEC.md | JAVA-MUST-0003 | {合规/偏差} |
| SEC.md | JAVA-MUST-0004 | {合规/偏差} |
| SEC.md | JAVA-MUST-0005 | {合规/偏差} |

### 自检

- [x] Task 1: {描述} - {结果}
- [x] Task 2: {描述} - {结果}

### 观察

- 发现: {现象} → {原因}
- 异常: {异常} → {处理}


---

## deliver | {开始} - {结束} | {✅/完成}

### 输入

- State 关注: 验收、交付
- 阶段知识: `docs/knowledge/stages/deliver.md`

### Handoff

- 阶段: implement
- 核心产出: {已实现的代码文件}
- 自检结果: {Task 完成状态}

### 过程

- 步骤: {动作} → {结果}

### 输出

- PR: {PR 链接}
- 验收结果: {通过/待改进}

### 验证

#### 轨道 A：流程合规
SR-0 ~ SR-8 | {合规/偏差}: {详情}

#### 轨道 B：产出质量
QR-0 ~ QR-3 | {合规/偏差}: {详情}

### 观察

- 发现: {现象} → {原因}
- 异常: {异常} → {处理}


---

## Iteration | {日期}

### 目标评分

| 目标 | 评分 |
|------|------|
| 1. 编排正确性 | /10 |
| 2. 子 agent 质量 | /10 |
| 3. 知识传递有效性 | /10 |
| 4. 产出可交付性 | /10 |
| 总评 | /10 |

### Per-stage 耗时

| 阶段 | 耗时 |
|------|------|
| understand | min |
| design | min |
| plan | min |
| implement | min |
| deliver | min |
| 总计 | min |

### 进化指标

| 指标 | 数值 |
|------|------|
| Pipeline 版本 | v{N} |
| SR 合规率 | X% |
| QR 合规率 | X% |
| 补读率 | X% |
| 阶段日志完整率 | X% |

### 退化分析

- {分析内容}

### 下次观察

- {观察点}