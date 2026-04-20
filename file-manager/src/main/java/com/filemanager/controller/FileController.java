package com.filemanager.controller;

import com.filemanager.model.FileInfo;
import com.filemanager.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件管理接口
 * 遵循 CLAUDE.md 架构约束：只做参数校验和响应格式化，不包含业务逻辑
 */
@RestController
@RequestMapping("/api/file")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private FileService fileService;


    /**
     * 上传文件
     * POST /api/file/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "parentId", required = false, defaultValue = "0") Long parentId,
            @RequestParam(value = "userId", required = false, defaultValue = "1") Long userId) {

        try {
            Long actualParentId = (parentId == null || parentId == 0) ? null : parentId;
            FileInfo fileInfo = fileService.uploadFile(file, actualParentId, userId);
            return ResponseEntity.ok(buildSuccess(fileInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 获取文件列表
     * GET /api/file/list?parentId=1&userId=1
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getFileList(
            @RequestParam(value = "parentId", required = false, defaultValue = "0") Long parentId,
            @RequestParam(value = "userId", required = false, defaultValue = "1") Long userId) {

        try {
            Long actualParentId = (parentId == null || parentId == 0) ? null : parentId;
            List<FileInfo> files = fileService.getFileList(actualParentId, userId);
            return ResponseEntity.ok(buildSuccess(files));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 搜索文件
     * GET /api/file/search?keyword=xxx&userId=1
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchFiles(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "userId", required = false, defaultValue = "1") Long userId) {

        try {
            List<FileInfo> files = fileService.searchFiles(keyword, userId);
            return ResponseEntity.ok(buildSuccess(files));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 删除文件（软删除）
     * DELETE /api/file/delete/1
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable Long id) {
        try {
            fileService.deleteFile(id);
            return ResponseEntity.ok(buildSuccess(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 批量删除文件
     * DELETE /api/file/delete-batch?ids=1,2,3
     */
    @DeleteMapping("/delete-batch")
    public ResponseEntity<Map<String, Object>> deleteFiles(@RequestParam("ids") String ids) {
        try {
            List<Long> idList = parseIds(ids);
            fileService.deleteFiles(idList);
            return ResponseEntity.ok(buildSuccess(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 恢复文件
     * POST /api/file/restore/1
     */
    @PostMapping("/restore/{id}")
    public ResponseEntity<Map<String, Object>> restoreFile(@PathVariable Long id) {
        try {
            fileService.restoreFile(id);
            return ResponseEntity.ok(buildSuccess(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 重命名文件
     * PUT /api/file/rename
     */
    @PutMapping("/rename")
    public ResponseEntity<Map<String, Object>> renameFile(@RequestBody Map<String, Object> request) {
        try {
            Long id = Long.parseLong(request.get("id").toString());
            String newName = request.get("name").toString();
            FileInfo fileInfo = fileService.renameFile(id, newName);
            return ResponseEntity.ok(buildSuccess(fileInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 移动文件
     * PUT /api/file/move
     */
    @PutMapping("/move")
    public ResponseEntity<Map<String, Object>> moveFile(@RequestBody Map<String, Object> request) {
        try {
            Long id = Long.parseLong(request.get("id").toString());
            Long newParentId = Long.parseLong(request.get("parentId").toString());
            FileInfo fileInfo = fileService.moveFile(id, newParentId);
            return ResponseEntity.ok(buildSuccess(fileInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 下载文件
     * GET /api/file/download/1
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            FileInfo fileInfo = fileService.getFileById(id);
            if (fileInfo == null) {
                throw new RuntimeException("文件不存在");
            }

            if (fileInfo.getDeleted() == 1) {
                return ResponseEntity.badRequest().build();
            }

            String filePath = fileService.getDownloadPath(id);
            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = determineContentType(fileInfo.getFormat());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileInfo.getName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新文件内容
     * PUT /api/file/update-content
     */
    @PutMapping("/update-content")
    public ResponseEntity<Map<String, Object>> updateContent(@RequestBody Map<String, Object> request) {
        try {
            Long id = Long.parseLong(request.get("id").toString());
            String content = request.get("content").toString();
            FileInfo fileInfo = fileService.updateContent(id, content);
            return ResponseEntity.ok(buildSuccess(fileInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
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