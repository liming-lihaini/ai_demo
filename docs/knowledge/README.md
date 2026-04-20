# 知识库目录

## 目录结构

```
docs/knowledge/
├── rulers/                 # 规则知识（规则验证）
│   ├── Code.md            # 编码规则
│   ├── SEC.md             # 安全规则
│   └── ...
├── stages/                # 阶段知识（各阶段专用）
│   ├── understand.md      # understand 阶段知识
│   ├── design.md          # design 阶段知识
│   ├── plan.md            # plan 阶段知识
│   ├── implement.md       # implement 阶段知识
│   └── deliver.md         # deliver 阶段知识
├── domain/                # 领域知识
│   └── ...
└── README.md             # 本文件
```

## 使用说明

### 规则知识 (rulers/)

存放企业级编码规范、安全规范等规则文件。每个规则文件包含：

- 规则 ID 和名称
- 规则等级（FORBIDDEN/MUST/SHOULD）
- 适用场景
- 合规示例 vs 违规示例
- 修复指引

### 阶段知识 (stages/)

每个阶段对应的知识文件，包含：

- 阶段目标
- 关注重点
- 关键任务
- 输出要求

### 领域知识 (domain/)

按业务领域组织的知识文件，如：

- 应用架构.md
- 业务流程.md
- 业务规则.md

## 知识加载

通过 `/pipeline-load {阶段名}` 命令加载知识，加载时会：

1. 加载阶段知识文件
2. 匹配相关规则知识
3. 输出加载信息到日志
