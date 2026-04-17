# ADR: Office 文档转换方案

## 背景

Issue-3 要求支持 DOC、DOCX、XLS、XLSX、PPT、PPTX 转换为 PDF 后预览。需求明确：
- 单个 ≤20MB 文件转换时间 ≤5秒
- ≥20MB 显示加载进度

当前项目使用 SpringBoot + Java 17，已有 PDFBox 处理 PDF。需要选择 Office 转 PDF 的技术方案。

## 备选方案

### 方案 A: Apache POI + PDFBox（纯 Java 方案）
- 优势: 纯 Java 实现，无外部依赖；项目已引入 PDFBox；易于部署
- 劣势: 对复杂格式支持可能不完整；PPT/PPTX 支持较弱

### 方案 B: LibreOffice 无头模式（命令行）
- 优势: 转换质量高，格式兼容性最好；支持所有 Office 格式
- 劣势: 需要安装 LibreOffice 环境；桌面端应用需携带或检测 LibreOffice 安装；跨平台兼容性复杂

### 方案 C: Apache POI + iText（仅文本提取）
- 优势: 纯 Java，部署简单
- 劣势: 只能提取文本，无法保留原格式；不满足预览需求

### 方案 D: 使用前端预览（如 mammoth.js + pdf.js）
- 优势: 减轻服务端压力
- 劣势: 复杂格式转换效果差；桌面端仍需处理大文件

## 决策

选择方案 A（Apache POI + PDFBox），因为：
1. MVP 阶段优先保证可部署性，LibreOffice 需要额外环境依赖
2. 项目已有 PDFBox 依赖，只需引入 Apache POI
3. 常见文档（DOCX/XLSX）转换效果可接受
4. 可降级处理：转换失败时提示用户下载查看

## 否决项

- 方案 B: LibreOffice 环境依赖是 Issue 中标注的阻塞问题，且桌面端需要额外安装/打包，MVP 阶段应避免
- 方案 C: 仅提取文本无法满足预览需求
- 方案 D: 前端处理大文件时性能不佳，且格式兼容性不如服务端

## 影响

### 新增依赖（pom.xml）
```xml
<!-- Apache POI for Office 文件处理 -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.2.5</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

### 新增文件
- `file-manager/src/main/java/com/filemanager/util/OfficeConverter.java` - Office 转 PDF 核心类

### 降级策略
- 转换超时（>10秒）或失败时，返回特定错误码
- 前端显示："文档转换失败，请尝试下载后查看"
- 支持手动下载原始文件

## 不需要决策的维度

- 数据模型: 无需新增数据库表
- 安全: 转换在服务端沙箱执行，无额外安全风险
- 异步处理: MVP 阶段同步处理即可，转换超时控制通过后端超时实现