# Stage: Deliver

职责：提交代码、创建 PR。前面做对了这里很简单。

## Act

> 执行 `/pipeline-load deliver` 进行知识加载

1. 最终验证：
   - `make test && make lint` — 必须零错误（代码交付）
   - 如无 Makefile（pipeline/文档交付）：跳过 make 命令，改为人工检查文件内容和格式
   - `make test-coverage` — 确认覆盖率（代码交付）
   - `git diff --name-only` — 确认无无关文件
   - `git diff --name-only` — 确认知识文件变更（如有）已包含在 diff 中
   - 如有未解决的 🔴 Open Questions → 暂停交付，升级人工

2. 更新 Issue Summary（docs/issues/issue-{issue编号}.md）：
   - 勾选 acceptance criteria
   - 记录实现摘要（做了什么、关键决策、与计划的偏差）

3. 提交推送：
   - `git add .`
   - `git commit -m "feat({scope}): {description}"`
   - `git push origin feature/issue-{N}`

4. 创建 PR：
   - `gh pr create --base main --title "feat: {description}" --body-file /tmp/pr-body.md`

### PR 描述模板

```markdown
## 变更摘要
{2-3 句话描述本次变更做什么、为什么}

## 涉及模块
- {模块1}: {做了什么}
- {模块2}: {做了什么}

## 关联
- Closes #{Issue 编号}
- ADR: {链接到相关 ADR}

## 知识文件变更
- {docs/knowledge/xxx.md}: {新增/更新了什么}（如无则写"无"）

## 测试
- 新增测试: {列出新增的测试文件或函数}
- 运行结果: `make test && make lint` 通过

## 与计划的偏差
- {如有偏差，说明原因}
- {如无偏差，写"无偏差"}
```

5. PR 创建后或者创建PR失败，在 pipeline-state-{issue编号}.md Deliver section 记录 PR 链接

## Verify

- [ ] 最终验证通过：代码交付时 `make test && make lint` 零错误；非代码交付时文件内容和格式正确
- [ ] commit message 符合 conventional commits 格式（feat/fix/refactor/{scope}）
- [ ] PR 描述完整：变更摘要 + 模块列表 + 知识变更 + 测试结果 + 偏差说明
- [ ] PR 关联正确 Issue（Closes #N）
- [ ] git diff 无无关文件
- [ ] Issue Summary 已更新
- [ ] 知识文件变更（如有）已包含在 diff 中
- [ ] 无未解决的 🔴 Open Questions

## 异常处理

- make test 失败 → 修复后重跑，不跳过
- PR 创建失败 → 检查 gh CLI 认证，或手动创建
- 知识文件变更未包含 → git add 确认已暂存
