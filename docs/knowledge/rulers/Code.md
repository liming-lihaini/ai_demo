# 后端Java AI Coding编码规则

本文档专为后端Java开发场景制定，规范AI编程辅助工具生成的Java代码质量、安全性及合规性，确保代码符合企业研发标准。

## 规则总览

| 序号 | 规则ID | 所属业务域 | 风险标签 | 规则等级 | 规则概要 |
|------|--------|------------|----------|----------|----------|
| 1 | JAVA-MUST-0001 | 通用 | 【安全红线】 | FORBIDDEN | 详见下方详细说明 |
| 2 | JAVA-MUST-0002 | 通用 | 【安全红线】 | FORBIDDEN | 详见下方详细说明 |
| 3 | JAVA-MUST-0003 | 通用 | 【关键业务】 | MUST | 详见下方详细说明 |
| 4 | JAVA-MUST-0004 | 通用 | 【安全红线】 | MUST | 详见下方详细说明 |
| 5 | JAVA--MUST-0005 | 通用 | 【通用研发】 | MUST | 详见下方详细说明 |
| 6 | JAVA-MUST-0001 | 通用 | 【安全红线】 | FORBIDDEN | 详见下方详细说明 |
| 7 | JAVA-BACKEND-SHOULD-0006 | 通用 | 【关键业务】 | SHOULD | 详见下方详细说明 |

---
## 详细规则说明

### 1. JAVA-MUST-0001 - 【安全红线】

**所属业务域**: 通用

**规则等级**: FORBIDDEN - 【禁止级】绝对禁止，任何情况下不得违反，违反将导致严重安全风险或业务故障

**适用场景**: 

代码生成、CR、安全审计

**合规示例**: 

@Select("SELECT * FROM user WHERE id = #{userId}")
PreparedStatement ps = conn.prepareStatement("SELECT * FROM user WHERE id = ?");

**违规示例**: 

String sql = "SELECT * FROM user WHERE id = " + userId;
@Select("SELECT * FROM user WHERE id = ${userId}")

**修复指引**: 

1. 将所有动态SQL拼接改为#{}占位符或?占位符；
2. 若使用JDBC，改用PreparedStatement；
3. 若为动态SQL（如MyBatis的<if>），确保条件参数使用#{}而非${}。

**优先级**: 

1

**生效日期**: 

2026-01-01

---

### 2. JAVA-MUST-0002 - 【安全红线】

**所属业务域**: 通用

**规则等级**: FORBIDDEN - 【禁止级】绝对禁止，任何情况下不得违反，违反将导致严重安全风险或业务故障

**适用场景**: 

代码生成、CR、安全审计

**合规示例**: 

@Value("${db.password}") private String password;
环境变量：DB_PASSWORD

**违规示例**: 

String password = "123456";
jdbc:mysql://localhost:3306/db?user=root&password=root

**修复指引**: 

1. 移除硬编码敏感信息；
2. 将敏感配置迁移至配置中心（如Apollo、Nacos）或环境变量；
3. 使用@Value或Environment读取配置。

**优先级**: 

1

**生效日期**: 

2026-01-01

---

### 3. JAVA-MUST-0003 - 【关键业务】

**所属业务域**: 通用

**规则等级**: MUST - 【必须级】必须严格遵守，无特殊例外情况，确保代码基础质量和安全

**适用场景**: 

代码生成、CR

**合规示例**: 

@Transactional(rollbackFor = Exception.class)
public void transfer(Account from, Account to, BigDecimal amount)

**违规示例**: 

无@Transactional注解执行多次写操作；
@Transactional未指定rollbackFor导致检查异常不回滚。

**修复指引**: 

1. 在Service层方法添加@Transactional(rollbackFor = Exception.class)；
2. 确保方法内所有数据库操作在同一个事务上下文中；
3. 避免在事务内进行RPC调用或大量非DB操作。

**优先级**: 

2

**生效日期**: 

2026-01-01

---

### 4. JAVA-MUST-0004 - 【安全红线】

**所属业务域**: 通用

**规则等级**: MUST - 【必须级】必须严格遵守，无特殊例外情况，确保代码基础质量和安全

**适用场景**: 

代码生成、CR、安全审计

**合规示例**: 

log.info("用户注册：{}", DesensitizationUtils.mobile(mobile));
输出：138****5678

**违规示例**: 

log.info("用户注册：{}", mobile);

**修复指引**: 

1. 识别日志打印中的敏感字段；
2. 调用统一脱敏工具类对敏感字段进行脱敏；
3. 确保脱敏逻辑不影响业务调试核心信息。

**优先级**: 

2

**生效日期**: 

2026-01-01

---

### 5. JAVA--MUST-0005 - 【通用研发】

**所属业务域**: 通用

**规则等级**: MUST - 【必须级】必须严格遵守，无特殊例外情况，确保代码基础质量和安全

**适用场景**: 

代码生成、CR

**合规示例**: 

log.info("订单创建成功，orderId={}", orderId);
log.error("支付回调处理失败，orderId={}", orderId, e);

**违规示例**: 

log.error("用户登录成功"); 不应使用error级别
log.info(e.getMessage(), e); 异常应使用error

**修复指引**: 

1. 评估日志重要性；
2. 关键业务流程使用info；
3. 异常信息使用error并打印堆栈。

**优先级**: 

3

**生效日期**: 

2026-01-01

---

### 6. JAVA-MUST-0001 - 【安全红线】

**所属业务域**: 通用

**规则等级**: FORBIDDEN - 【禁止级】绝对禁止，任何情况下不得违反，违反将导致严重安全风险或业务故障

**适用场景**: 

代码生成、CR、安全审计

**合规示例**: 

@Select("SELECT * FROM user WHERE id = #{userId}")
PreparedStatement ps = conn.prepareStatement("SELECT * FROM user WHERE id = ?");

**违规示例**: 

String sql = "SELECT * FROM user WHERE id = " + userId;
@Select("SELECT * FROM user WHERE id = ${userId}")

**修复指引**: 

1. 将所有动态SQL拼接改为#{}占位符或?占位符；
2. 若使用JDBC，改用PreparedStatement；
3. 若为动态SQL（如MyBatis的<if>），确保条件参数使用#{}而非${}。

**优先级**: 

1

**生效日期**: 

2026-01-01

---

### 7. JAVA-BACKEND-SHOULD-0006 - 【关键业务】

**所属业务域**: 通用

**规则等级**: SHOULD - 【建议级】建议优先遵守，特殊场景需经技术评审确认，提升代码规范性和可维护性

**适用场景**: 

代码生成、CR

**合规示例**: 

MDC.put("traceId", traceId);
log.info("处理请求，traceId={}", MDC.get("traceId"));

**违规示例**: 

关键链路未输出可追踪标识

**修复指引**: 

1. 接入分布式链路追踪框架；
2. 在日志配置中打印traceId；
3. 跨服务调用透传traceId。

**优先级**: 

4

**生效日期**: 

2026-01-01

---

## 规则等级说明

| 等级 | 标识 | 强制程度 | 适用场景 |
|------|------|----------|----------|
| 禁止级 | FORBIDDEN | 绝对禁止 | 涉及安全红线、可能导致系统崩溃或数据泄露的操作 |
| 必须级 | MUST | 强制遵守 | 保障代码安全性、稳定性、可维护性的基础要求 |
| 建议级 | SHOULD | 推荐遵守 | 提升代码质量、优化性能的最佳实践 |

## 使用指南

1. **适用范围**: 所有使用AI编程辅助工具（如Copilot、CodeGeeX等）生成后端Java代码的场景。
2. **执行要求**: 
   - 开发人员在使用AI工具生成代码后，需对照本规则进行自查。
   - 代码评审环节需将本规则作为评审标准之一，不符合规则的代码需整改后再合并。
   - 定期对AI生成代码的合规性进行抽检，确保规则落地执行。
3. **更新机制**: 本规则将根据Java技术栈更新、安全漏洞变化及业务需求调整，更新记录将同步至文档版本历史。
