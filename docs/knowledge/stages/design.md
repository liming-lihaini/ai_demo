# 设计阶段知识

## 1. 阶段目标

产出**技术决策记录（ADR）**，解决 Open Questions，将需求转化为具体设计方案。

---

## 2. 技术架构

### 2.1 分层架构

```
Controller → Service → Repository → Model
```

| 层级 | 职责 | 禁止操作 |
|------|------|----------|
| Controller | 参数校验 + 响应格式化 + 参数组装 | 禁止执行业务逻辑 |
| Service | 业务逻辑核心，事务边界，可调用多个Repository | 禁止直接访问数据库 |
| Repository | 数据访问层，封装SQL查询 | 禁止执行业务逻辑 |
| Model | 数据模型定义，仅getter/setter | 禁止业务逻辑 |

### 2.2 响应格式

```json
{
  "code": 0,
  "message": "成功",
  "data": {}
}
```

错误码规范:
- 0: 成功
- 1: 参数错误
- 2: 业务错误
- 3: 系统错误

### 2.3 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Element Plus + Vite |
| 后端 | Spring Boot 2.7 + Java 17 + MyBatis-Plus |
| 数据库 | SQLite 3.40+ |

---

## 3. ADR 规范

### 3.1 ADR 结构

```markdown
# ADR-{N}: {决策标题}

## 背景
{描述需要决策的问题}

## 方案
### 方案 A
- 优点: ...
- 缺点: ...

### 方案 B
- 优点: ...
- 缺点: ...

## 选择
选择: 方案 {X}
原因: {引用项目实际}

## 影响
{对后续实现的影响}
```

### 3.2 决策类型

| 类型 | 示例 |
|------|------|
| 数据模型 | 新增字段、表结构变更 |
| API 设计 | 接口路径、请求响应格式 |
| 架构设计 | 分层调整、组件拆分 |
| 定时任务 | 调度策略、执行逻辑 |
| 索引设计 | 数据库索引、查询优化 |

### 3.3 ADR 位置

- 路径: `docs/current/decisions/adr-{主题}.md`
- 命名: adr-{issue编号}-{主题}.md

---

## 4. 设计约束

### 4.1 安全约束（SEC.md）

| 规则ID | 约束内容 |
|--------|----------|
| JAVA-MUST-0001 | 参数化查询，禁止SQL字符串拼接 |
| JAVA-MUST-0002 | 禁止硬编码敏感信息（密码、密钥） |
| JAVA-MUST-0003 | 核心安全模块禁止AI生成代码 |
| DATABASE-MUST-0001 | 数据库命名符合规范 |

### 4.2 编码约束（Code.md）

| 规则ID | 约束内容 |
|--------|----------|
| 分层约束 | Controller → Service → Repository → Model |
| 事务边界 | Service 层方法开启事务 |
| API 设计 | RESTful 风格 |
| 响应格式 | {code, message, data} |

### 4.3 边界合规

设计方案必须：
- 在 Boundaries 范围之内
- 不违反已有模块的设计决策
- 符合项目技术栈约束

---

## 5. 本阶段执行标准

### 5.1 输入要求

| 输入项 | 来源 | 说明 |
|--------|------|------|
| Task | pipeline-state | 需求理解结果 |
| Boundaries | pipeline-state | 范围约束 |
| Open Questions | pipeline-state | 待确认问题 |
| 阶段知识 | docs/knowledge/stages/design.md | 本阶段执行指引 |
| 规则知识 | docs/knowledge/rulers/* | 约束文件 |

### 5.2 执行步骤

1. **分析需求** - 理解 Task 和验收条件
2. **识别决策点** - 列出需要技术决策的点
3. **制定备选方案** - 每个决策点至少2个方案
4. **选择最优方案** - 结合项目实际选择
5. **编写 ADR** - 记录决策过程和理由
6. **验证约束** - 确保符合规则知识

### 5.3 输出要求

| 输出项 | 内容 |
|--------|------|
| ADR 文档 | docs/current/decisions/adr-{N}-{主题}.md |
| Decisions | pipeline-state 中的 Decisions section |
| Open Questions | 解决状态更新 |

### 5.4 验收条件

| 条件 | 判定标准 |
|------|----------|
| 决策完整 | 所有 Open Questions 都有 ADR 决策 |
| 备选充分 | 每个决策至少2个备选方案及对比 |
| 边界合规 | 所有设计在 Boundaries 范围内 |
| 约束合规 | 通过 SEC.md 和 Code.md 验证 |
| 信息充分 | plan 阶段可基于 ADR 开始实现计划 |

---

## 6. 设计检查清单

### 6.1 架构检查

- [ ] Controller 层仅做参数校验和响应封装
- [ ] Service 层处理业务逻辑
- [ ] Repository 层封装数据访问
- [ ] 无跨层调用

### 6.2 API 检查

- [ ] 路径符合 RESTful 规范
- [ ] 请求响应格式统一
- [ ] 错误码定义清晰

### 6.3 安全检查

- [ ] 无 SQL 注入风险
- [ ] 无硬编码敏感信息
- [ ] 路径穿越防护

### 6.4 数据检查

- [ ] 新增字段有明确类型和约束
- [ ] 索引设计合理
- [ ] 软删除逻辑正确

---

## 7. 参考资源

- 应用架构: `docs/knowledge/应用架构.md`
- 编码规范: `docs/knowledge/rulers/Code.md`
- 安全规范: `docs/knowledge/rulers/SEC.md`
- 阶段模板: `pipeline-log-template.md`
