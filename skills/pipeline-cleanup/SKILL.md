---
name: pipeline-cleanup
description: PR 合并后清理 Issue 相关资源，更新 Issue 状态
version: 1.0
author: Local Skill
---

# Pipeline Cleanup

PR 合并后执行。清理 Issue 相关资源，更新 Issue 状态。

## 输入

$ARGUMENTS = Issue 编号（如 6）

## 执行流程

### 1. 确认 PR 已合并

```bash
gh pr list --state merged --head feature/issue-$ARGUMENTS
```

如未合并 → 停止，提示等待合并

### 2. 拉取合并代码

```bash
git checkout main && git pull origin main
```

### 3. 更新 Issue Summary 元数据

编辑 `docs/issues/issue-$ARGUMENTS-*.md`：
- status: closed
- closed: {日期}
- PR: #{PR 编号}

提交：
```bash
git add docs/issues/issue-$ARGUMENTS-* && git commit -m "docs: close issue #$ARGUMENTS" && git push origin main
```

### 4. 关闭 GitHub Issue（如适用）

```bash
gh issue close $ARGUMENTS --comment "Closed via PR #{PR 编号}"
```

### 5. 清理 worktree

```bash
git worktree remove .claude/worktrees/issue-$ARGUMENTS
git branch -d feature/issue-$ARGUMENTS
```

## Verify

验证清理完成：

- [ ] PR 状态为 merged
- [ ] Issue 状态为 closed
- [ ] Issue Summary 元数据已更新
- [ ] Worktree 已移除
- [ ] 本地分支已删除

## 输出

输出清理结果：
```
Pipeline Cleanup 完成
- Issue #{$ARGUMENTS} 已关闭
- Worktree 已清理
- 分支已删除
```