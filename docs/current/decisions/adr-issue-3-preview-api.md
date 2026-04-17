# ADR: 预览模块 API 设计

## 背景

Issue-3 文件预览功能需要提供后端 API 供前端调用。参考项目现有的 Controller-Service-Repository 分层架构，需要设计合理的 API 路径和响应格式。

## 备选方案

### 方案 A: 集中式 Preview API
- 优势: 统一入口，便于管理；一个 Controller 处理所有预览请求
- 劣势: 方法较多，代码可能较长

### 方案 B: 分散式 API（按类型分组）
- 优势: 每个文件类型独立 API，职责清晰
- 劣势: 分散在多个 Controller

### 方案 C: 混合方案
- 优势: 集中式 URL 获取，分散式内容获取
- 劣势: 需要维护两种风格

## 决策

选择方案 A（集中式 PreviewController），因为：
1. 预览逻辑相对统一，集中管理降低维护成本
2. 符合 RESTful 风格，路径清晰
3. 便于后续添加预览权限控制

## 决策详情

### API 设计

| 接口 | 方法 | 路径 | 功能 |
|------|------|------|------|
| 获取预览信息 | GET | /api/preview/{fileId} | 返回文件预览所需信息（类型、URL、是否需要转换） |
| 获取预览内容 | GET | /api/preview/content/{fileId} | 返回文件预览内容流（图片/音频/视频/TXT/MD） |
| 获取PDF预览 | GET | /api/preview/pdf/{fileId} | 返回 PDF 预览（直接PDF或转换后） |
| 发起转换 | POST | /api/preview/convert/{fileId} | 触发 Office 转 PDF（可选，后端自动处理） |
| 获取缩略图 | GET | /api/preview/thumb/{fileId} | 返回文件缩略图 |

### 响应格式

```json
// GET /api/preview/{fileId} 响应
{
  "code": 200,
  "message": "success",
  "data": {
    "fileId": 1,
    "fileName": "文档.pdf",
    "format": "pdf",
    "previewType": "pdf",  // pdf, image, audio, video, text, convert
    "url": "/api/preview/content/1",  // 预览内容 URL
    "size": 1024000,
    "thumbUrl": "/api/preview/thumb/1",  // 缩略图 URL（图片/视频）
    "needConvert": false,  // 是否需要转换
    "convertStatus": null  // pending, converting, completed, failed
  }
}
```

### 文件类型映射

| 文件格式 | previewType | 处理方式 |
|----------|-------------|----------|
| pdf | pdf | 直接返回流 |
| txt | text | 直接返回文本 |
| md | text | Markdown 转 HTML 后返回 |
| jpg/jpeg/png/gif/bmp | image | 直接返回流 |
| mp3/wav | audio | 直接返回流 |
| mp4/avi | video | 直接返回流 |
| doc/docx/xls/xlsx/ppt/pptx | convert | 转换为 PDF 后返回 |

## 否决项

- 方案 B: 预览功能相对独立，分散多个 Controller 增加复杂度
- 方案 C: 混合方案在初期会导致 API 风格不一致

## 影响

### 新增文件
- `file-manager/src/main/java/com/filemanager/controller/PreviewController.java`
- `file-manager/src/main/java/com/filemanager/service/PreviewService.java`
- `file-manager/src/main/webapp/src/api/preview.js`

### 修改文件
- 复用现有 FileInfoService 获取文件信息

## 不需要决策的维度

- 缓存策略: MVP 阶段不需要复杂缓存，直接读取文件系统
- 异步转换: 可在 Service 层内部处理，API 保持同步（超时控制）