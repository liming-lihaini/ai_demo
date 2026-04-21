# 实现阶段知识

## 1. 阶段目标

按照计划实现功能，遵循架构约束，产出**可运行的代码**。

---

## 2. 架构约束

### 2.1 分层约束

```
Controller → Service → Repository → Model
```

| 层级 | 职责 | 禁止操作 |
|------|------|----------|
| Controller | 参数校验 + 响应格式化 | 禁止执行业务逻辑 |
| Service | 业务逻辑核心，事务边界 | 禁止直接访问数据库 |
| Repository | 数据访问层，封装SQL | 禁止执行业务逻辑 |
| Model | 数据模型定义 | 禁止业务逻辑 |

### 2.2 约束规则

| 约束 | 说明 |
|------|------|
| 事务 | Service 层开启事务，Repository 不开启 |
| 校验 | Controller 使用 @Valid 注解 |
| 异常 | Service 抛异常，Controller 统一捕获 |
| 响应格式 | 统一 {code, message, data} |

### 2.3 响应格式

```json
// 成功
{
  "code": 0,
  "message": "成功",
  "data": {}
}

// 失败
{
  "code": 1,
  "message": "错误信息"
}
```

---

## 3. 实现流程

### 3.1 Task 执行顺序

按依赖关系顺序执行：
1. 先执行无依赖的 Task
2. 再执行依赖已完成的 Task
3. 最后执行前置 Task 都完成的 Task

### 3.2 代码风格

参考同模块现有文件：
- 命名规范（方法名、变量名）
- 注释风格
- 日志级别
- 异常处理方式

### 3.3 文件位置

| 类型 | 路径 |
|------|------|
| 后端 Controller | file-manager/src/main/java/com/filemanager/controller/ |
| 后端 Service | file-manager/src/main/java/com/filemanager/service/ |
| 后端 Repository | file-manager/src/main/java/com/filemanager/repository/ |
| 后端 Model | file-manager/src/main/java/com/filemanager/model/ |
| 前端 Views | file-manager/src/main/webapp/src/views/ |
| 前端 Components | file-manager/src/main/webapp/src/components/ |
| 前端 API | file-manager/src/main/webapp/src/api/ |

---

## 4. 约束验证

### 4.1 安全约束（SEC.md）

| 规则ID | 约束内容 | 验证方法 |
|--------|----------|----------|
| JAVA-MUST-0001 | 参数化查询 | 使用 #{} 而非 ${} |
| JAVA-MUST-0002 | 无硬编码敏感信息 | 密码/密钥通过配置读取 |
| JAVA-MUST-0003 | 事务正确使用 | @Transactional(rollbackFor = Exception.class) |
| JAVA-MUST-0004 | 日志脱敏 | 敏感字段打印前脱敏 |
| JAVA-MUST-0005 | 日志级别正确 | info/error 使用场景正确 |

### 4.2 编码约束（Code.md）

| 规则ID | 约束内容 |
|--------|----------|
| 分层约束 | Controller → Service → Repository → Model |
| 响应格式 | {code, message, data} 统一格式 |
| 异常处理 | Service 抛异常，Controller 统一捕获 |
| 参数校验 | Controller 使用 @Valid 注解 |

---

## 5. 本阶段执行标准

### 5.1 输入要求

| 输入项 | 来源 | 说明 |
|--------|------|------|
| Task 列表 | pipeline-state | 实现计划中的 Task |
| 实现计划 | docs/plans/ | 详细实现步骤 |
| ADR | docs/current/decisions/ | 技术决策详情 |
| References | pipeline-state | 参考的现有代码 |
| 阶段知识 | docs/knowledge/stages/implement.md | 本阶段执行指引 |

### 5.2 执行步骤

1. **按顺序执行 Task** - 遵循依赖关系
2. **编写代码** - 遵循分层约束和代码风格
3. **自检验证** - 每个 Task 完成后的检查
4. **更新 State** - 记录产出文件

### 5.3 输出要求

| 输出项 | 内容 |
|--------|------|
| 代码文件 | 新增/修改的代码文件 |
| 产出列表 | pipeline-state 中的产出文件列表 |

### 5.4 验收条件

| 条件 | 判定标准 |
|------|----------|
| Task 完成 | 所有 Task 都已完成 |
| 分层合规 | 无跨层调用，遵循分层约束 |
| 约束合规 | 通过 SEC.md 和 Code.md 验证 |
| 代码质量 | 参考现有代码风格，无硬编码 |
| 自检通过 | 每个 Task 有验证结果 |

---

## 6. 自检清单

### 6.1 功能自检

- [ ] Task 要求的代码已实现
- [ ] 对应的验收条件已满足

### 6.2 架构自检

- [ ] Controller 仅做参数校验和响应封装
- [ ] Service 处理业务逻辑
- [ ] Repository 封装数据访问
- [ ] 无跨层调用

### 6.3 安全自检

- [ ] 无 SQL 注入风险
- [ ] 无硬编码敏感信息
- [ ] 事务正确使用
- [ ] 日志无敏感信息泄露

### 6.4 质量自检

- [ ] 命名符合规范
- [ ] 代码风格一致
- [ ] 注释适度

---

## 7. 常见问题

### 7.1 实现问题

| 问题 | 处理方式 |
|------|----------|
| 参考文件不存在 | 参考同模块其他文件 |
| 技术方案不明确 | 回退到 design 阶段 |
| 依赖问题 | 调整 Task 执行顺序 |

### 7.2 合规问题

| 问题 | 处理方式 |
|------|----------|
| 分层违规 | 重构代码到正确层级 |
| 安全风险 | 修复安全问题后再继续 |
| 风格不一致 | 参考现有代码调整 |

---

## 8. 参考资源

- 应用架构: `docs/knowledge/应用架构.md`
- 编码规范: `docs/knowledge/rulers/Code.md`
- 安全规范: `docs/knowledge/rulers/SEC.md`
- 阶段模板: `pipeline-log-template.md`
