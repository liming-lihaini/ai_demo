# ADR: 大文件预览处理策略

## 背景

Issue-3 需求明确：
- 单个 ≤20MB 文件转换时间 ≤5秒
- ≥20MB 文件显示加载进度

对于大文件，服务端需要在返回文件内容时支持分块传输，前端需要显示加载进度。

## 备选方案

### 方案 A: 服务端分块流式传输 + 前端进度显示
- 优势: 利用 HTTP Range 支持断点续传；前端可显示加载进度
- 劣势: 需要前端配合处理流数据

### 方案 B: 客户端分片下载
- 优势: 支持大文件分片并行下载
- 劣势: 前端实现复杂，需要管理分片和合并

### 方案 C: 服务端压缩后传输
- 优势: 减少传输时间
- 劣势: 增加服务端压力，压缩需要时间

## 决策

选择方案 A（分块流式传输），因为：
1. HTTP 协议原生支持 Range，服务端实现相对简单
2. 前端可以通过监听 XMLHttpRequest 或 Fetch 的 progress 事件显示进度
3. 对于音视频，HTML5 原生支持流式播放，自动处理缓冲

## 实现细节

### 服务端实现

```java
// 使用 Spring 的 ResponseEntity 实现 Range 请求
@GetMapping("/preview/content/{fileId}")
public ResponseEntity<Resource> getPreviewContent(@PathVariable Long fileId) {
    File file = fileService.getFile(fileId);
    // 设置 Content-Range 头
    // 支持断点续传
}
```

### 大文件判断逻辑

```
file.size < 20MB → 正常加载
file.size >= 20MB → 显示加载进度条
```

### 前端实现

```javascript
// 使用 Axios 的 onDownloadProgress 回调
axios.get(url, {
  responseType: 'blob',
  onDownloadProgress: (progressEvent) => {
    const percent = Math.round((progressEvent.loaded / progressEvent.total) * 100);
    // 更新进度条
  }
})
```

### 音视频大文件处理

- 音视频使用 HTML5 Video/Audio 标签的流式播放
- 设置 `preload="metadata"` 预加载元数据
- 不需要完整下载即可播放

## 否决项

- 方案 B: 客户端分片实现复杂度高，MVP 阶段不需要
- 方案 C: 压缩会增加延迟，对于已压缩的媒体文件效果不明显

## 影响

### 前端修改
- PreviewDialog 增加 loading 状态和进度条
- 进度条使用 Element Plus 的 el-progress 组件

### 后端修改
- PreviewController 的内容接口支持 Range 请求
- 设置合理的 Content-Length 和 Content-Type

### 进度显示阈值
- 文件大小 ≥ 20MB (20 * 1024 * 1024 = 20971520 bytes) 时显示进度条

## 不需要决策的维度

- 分片上传: 大文件预览不需要分片上传功能
- 断点续传: MVP 阶段不需要断点续传，显示进度即可