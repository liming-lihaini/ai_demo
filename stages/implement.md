# Stage: Implement

职责：按执行计划逐个 Task 实现。严格遵守计划和 Boundaries。

## 实现原则

1. **模式优先** — 写代码前先读涉及目录下的现有文件，匹配已有的命名、结构和分层模式。不另起炉灶
2. **最小变更** — 只改当前 Task 需要改的。不在同一个 Task 里顺手重构相邻代码
3. **增量验证** — 每完成一个 Task 立即测试，不等全部写完。早期发现问题修复成本最低
4. **可回滚** — 每个 Task 一个 commit。如果某个 Task 出问题可以单独 revert，不影响其他 Task

## 测试原则

- 测试的是 Task 验收条件中描述的行为，不是测试实现细节
- 每个 Task 至少覆盖 happy path 和一个 error case
- 读项目中已有测试文件，匹配其框架、命名和组织方式
- 测试失败时不跳过、不忽略，修复后再继续

## Act

> 执行 `/pipeline-load implement` 进行知识加载

1. 按依赖顺序逐个 Task 实现
2. 每个 Task 开始前：读涉及目录下的现有文件，确认代码模式
3. 每个 Task 完成后立即写测试（遵循测试原则）
4. 每完成一个 Task 跑一次 make test && make lint
5. 每个 Task 一个 git commit（commit message 遵循项目已有 commit 风格）
6. 可并行的 Task 顺序不限，但同一文件避免并行修改
7. 全部完成后跑 make test-coverage 确认覆盖率

## 知识回流（条件触发，随 PR 提交）

检查是否触发以下任一条件，触发则更新对应文件：

| 条件 | 动作 |
|------|------|
| 新模块创建 | 创建 docs/knowledge/{module}.md |
| 模块公开 API 变更 | 更新对应 knowledge 文件的接口段 |
| 引入新外部依赖 | 更新 knowledge 的架构概览段 |
| TRD 与实现不一致 | 记入 Open Questions，不自行修改 TRD |

**不触发**：内部重构（接口不变）、新增测试、修复 bug（除非揭示文档错误）

7. 更新 pipeline-state-{issue编号}.md Current Stage

## 验证命令

- `make test && make lint` — 每 Task 必跑
- `make test-coverage` — 最终确认

## Verify

自检（每项必须有具体证据）：
- [ ] 文件位置正确：ls 检查产出文件路径符合项目结构
- [ ] 架构约束：import 关系无跨层调用
- [ ] 代码风格：与涉及目录下现有文件的命名和结构模式一致
- [ ] 编码规范：make lint 零错误
- [ ] 测试覆盖：每个 Task 的验收条件有对应测试
- [ ] 全部通过：make test && make lint 零错误
- [ ] 无范围外改动：git diff --name-only 对照 Boundaries
- [ ] commit 规范：每个 Task 一个 commit，message 风格与项目已有 commit 一致

## 异常处理

- 计划有遗漏 → 记入 Open Questions，不自行添加
- 设计决策不可行 → 记入 Open Questions + 暂停
- 测试失败 → 修复后重跑，不跳过
- lint 失败 → 修复后重跑，不忽略
