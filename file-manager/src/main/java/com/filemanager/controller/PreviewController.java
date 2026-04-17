package com.filemanager.controller;

import com.filemanager.dto.PreviewInfo;
import com.filemanager.model.FileInfo;
import com.filemanager.service.FileService;
import com.filemanager.service.PreviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预览接口
 * 遵循 CLAUDE.md 架构约束：只做参数校验和响应格式化，不包含业务逻辑
 */
@RestController
@RequestMapping("/api/preview")
@CrossOrigin(origins = "*")
public class PreviewController {

    private static final Logger logger = LoggerFactory.getLogger(PreviewController.class);

    @Autowired
    private PreviewService previewService;

    @Autowired
    private FileService fileService;

    /**
     * 获取预览信息
     * GET /api/preview/{fileId}
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<Map<String, Object>> getPreviewInfo(@PathVariable Long fileId) {
        try {
            PreviewInfo info = previewService.getPreviewInfo(fileId);
            return ResponseEntity.ok(buildSuccess(info));
        } catch (Exception e) {
            logger.error("获取预览信息失败: fileId={}, error={}", fileId, e.getMessage());
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 获取文件内容流（直接返回文件内容）
     * GET /api/preview/content/{fileId}
     */
    @GetMapping("/content/{fileId}")
    public ResponseEntity<Resource> getPreviewContent(@PathVariable Long fileId) {
        try {
            FileInfo fileInfo = fileService.getFileById(fileId)
                    .orElseThrow(() -> new RuntimeException("文件不存在"));

            if (fileInfo.getDeleted() == 1) {
                return ResponseEntity.badRequest().build();
            }

            Resource resource = previewService.getPreviewContent(fileId);
            String contentType = determineContentType(fileInfo.getFormat());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + fileInfo.getName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("获取预览内容失败: fileId={}, error={}", fileId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取 PDF 预览内容（适用于 PDF 或转换后的文本）
     * GET /api/preview/pdf/{fileId}
     */
    @GetMapping("/pdf/{fileId}")
    public ResponseEntity<byte[]> getPdfPreview(@PathVariable Long fileId) {
        try {
            FileInfo fileInfo = fileService.getFileById(fileId)
                    .orElseThrow(() -> new RuntimeException("文件不存在"));

            if (fileInfo.getDeleted() == 1) {
                return ResponseEntity.badRequest().build();
            }

            String format = fileInfo.getFormat().toLowerCase();

            // PDF 直接返回二进制
            if (format.equals("pdf")) {
                Resource resource = previewService.getPreviewContent(fileId);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\"" + fileInfo.getName() + "\"")
                        .body(resource.getContentAsByteArray());
            }

            // Office 文档转换为 HTML
            String htmlContent = previewService.getHtmlPreview(fileId);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlContent.getBytes());
        } catch (Exception e) {
            logger.error("获取 PDF 预览失败: fileId={}, error={}", fileId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取文本内容（纯文本，用于 TXT/MD 文件）
     * GET /api/preview/text/{fileId}
     */
    @GetMapping("/text/{fileId}")
    public ResponseEntity<String> getTextPreview(@PathVariable Long fileId) {
        try {
            FileInfo fileInfo = fileService.getFileById(fileId)
                    .orElseThrow(() -> new RuntimeException("文件不存在"));

            if (fileInfo.getDeleted() == 1) {
                return ResponseEntity.badRequest().build();
            }

            String textContent = previewService.getTextPreview(fileId);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/plain; charset=UTF-8"))
                    .body(textContent);
        } catch (Exception e) {
            logger.error("获取文本预览失败: fileId={}, error={}", fileId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取 HTML 预览内容（用于 Office 文档）
     * GET /api/preview/html/{fileId}
     */
    @GetMapping("/html/{fileId}")
    public ResponseEntity<String> getHtmlPreview(@PathVariable Long fileId) {
        try {
            FileInfo fileInfo = fileService.getFileById(fileId)
                    .orElseThrow(() -> new RuntimeException("文件不存在"));

            if (fileInfo.getDeleted() == 1) {
                return ResponseEntity.badRequest().build();
            }

            String htmlContent = previewService.getHtmlPreview(fileId);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlContent);
        } catch (Exception e) {
            logger.error("获取 HTML 预览失败: fileId={}, error={}", fileId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取缩略图
     * GET /api/preview/thumb/{fileId}
     */
    @GetMapping("/thumb/{fileId}")
    public ResponseEntity<Resource> getThumbnail(@PathVariable Long fileId) {
        try {
            FileInfo fileInfo = fileService.getFileById(fileId)
                    .orElseThrow(() -> new RuntimeException("文件不存在"));

            if (fileInfo.getDeleted() == 1) {
                return ResponseEntity.badRequest().build();
            }

            String format = fileInfo.getFormat().toLowerCase();
            if (!isImageFormat(format)) {
                return ResponseEntity.badRequest().build();
            }

            Resource resource = previewService.getThumbnail(fileId);
            String contentType = determineContentType(format);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            logger.error("获取缩略图失败: fileId={}, error={}", fileId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 判断是否为图片格式
     */
    private boolean isImageFormat(String format) {
        List<String> imageFormats = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");
        return imageFormats.contains(format.toLowerCase());
    }

    /**
     * 检查是否为大型文件（用于前端显示进度）
     * GET /api/preview/large/{fileId}
     */
    @GetMapping("/large/{fileId}")
    public ResponseEntity<Map<String, Object>> isLargeFile(@PathVariable Long fileId) {
        try {
            boolean isLarge = previewService.isLargeFile(fileId);
            long fileSize = previewService.getFileSize(fileId);

            Map<String, Object> data = new HashMap<>();
            data.put("isLarge", isLarge);
            data.put("fileSize", fileSize);

            return ResponseEntity.ok(buildSuccess(data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 批量获取预览信息
     * GET /api/preview/batch?ids=1,2,3
     */
    @GetMapping("/batch")
    public ResponseEntity<Map<String, Object>> getBatchPreviewInfo(@RequestParam("ids") String ids) {
        try {
            List<Long> idList = parseIds(ids);
            Map<Long, PreviewInfo> batchInfo = previewService.getBatchPreviewInfo(idList);
            return ResponseEntity.ok(buildSuccess(batchInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 解析 ID 字符串
     */
    private List<Long> parseIds(String ids) {
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();
    }

    /**
     * 根据扩展名确定 Content-Type
     */
    private String determineContentType(String format) {
        if (format == null) return "application/octet-stream";

        return switch (format.toLowerCase()) {
            case "pdf" -> "application/pdf";
            case "doc", "docx" -> "application/msword";
            case "xls", "xlsx" -> "application/vnd.ms-excel";
            case "txt" -> "text/plain";
            case "md" -> "text/markdown";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "mp4" -> "video/mp4";
            case "avi" -> "video/x-msvideo";
            default -> "application/octet-stream";
        };
    }

    /**
     * 构建成功响应
     */
    private Map<String, Object> buildSuccess(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "success");
        result.put("data", data);
        return result;
    }

    /**
     * 构建错误响应
     */
    private Map<String, Object> buildError(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 1);
        result.put("message", message);
        return result;
    }
}