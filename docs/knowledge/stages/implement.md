# Implement 阶段知识

## 阶段目标

按照计划实现功能，遵循架构约束，产出可运行的代码。

## 架构约束

| 约束 | 说明 |
|------|------|
| 分层 | Controller → Service → Repository → Model |
| 事务 | Service 层开启事务，Repository 不开启 |
| 校验 | Controller 使用 @Valid 注解 |
| 异常 | Service 抛异常，Controller 统一捕获 |
| 响应格式 | 统一 {code, message, data} |

## 实现检查清单

- [ ] 每个 Task 按依赖顺序执行
- [ ] 遵循分层约束，不跨层调用
- [ ] 使用现有代码风格（参考同模块其他文件）
- [ ] 单元测试覆盖核心逻辑（如有）
- [ ] 无硬编码敏感信息

## 代码风格

参考同模块现有文件：
- 命名规范（方法名、变量名）
- 注释风格
- 日志级别
- 异常处理方式

## 实现约束验证（验收条件）

实现产出必须通过以下约束文件的检查：

### 安全合规检查（SEC.md）
- [ ] 无 SQL 注入风险：使用 #{} 而非 ${}（JAVA-MUST-0001）
- [ ] 无硬编码敏感信息：密码/密钥通过配置读取（JAVA-MUST-0002）
- [ ] 事务正确使用：@Transactional(rollbackFor = Exception.class)（JAVA-MUST-0003）
- [ ] 日志脱敏：敏感字段打印前脱敏（JAVA-MUST-0004）
- [ ] 日志级别正确：info/error 使用场景正确（JAVA-MUST-0005）

### 编码规范检查（Code.md）
- [ ] 分层约束：Controller → Service → Repository → Model
- [ ] 响应格式统一：{code, message, data}
- [ ] 异常处理：Service 抛异常，Controller 统一捕获
- [ ] 参数校验：Controller 使用 @Valid 注解

## 输出

- 代码文件修改/新增
- pipeline-state-{issue编号}.md 更新
