package com.filemanager.controller;

import com.filemanager.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索接口
 * 遵循 CLAUDE.md 架构约束：只做参数校验和响应格式化，不包含业务逻辑
 */
@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 搜索文件和目录
     * GET /api/search?q=关键词&type=file&fileType=doc&userId=1&page=1&size=20
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "type", required = false, defaultValue = "all") String type,
            @RequestParam(value = "userId", required = false, defaultValue = "1") Long userId,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "startDate", required = false) Long startDate,
            @RequestParam(value = "endDate", required = false) Long endDate,
            @RequestParam(value = "minSize", required = false) Long minSize,
            @RequestParam(value = "maxSize", required = false) Long maxSize,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {
        try {
            Map<String, Object> result;

            if ("file".equals(type)) {
                List<Map<String, Object>> files = searchService.searchFiles(
                    keyword, userId, fileType, startDate, endDate, minSize, maxSize, page, size);
                result = new HashMap<>();
                result.put("files", files);
                result.put("total", files.size());
            } else if ("directory".equals(type)) {
                List<Map<String, Object>> directories = searchService.searchDirectories(keyword, userId, page, size);
                result = new HashMap<>();
                result.put("directories", directories);
                result.put("total", directories.size());
            } else {
                result = searchService.searchAll(keyword, userId, fileType, startDate, endDate, minSize, maxSize, page, size);
            }

            return ResponseEntity.ok(buildSuccess(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 获取搜索建议（自动补全）
     * GET /api/search/suggest?q=关键词&userId=1&limit=10
     */
    @GetMapping("/suggest")
    public ResponseEntity<Map<String, Object>> getSuggestions(
            @RequestParam(value = "q") String keyword,
            @RequestParam(value = "userId", required = false, defaultValue = "1") Long userId,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        try {
            List<String> suggestions = searchService.getSuggestions(keyword, userId, limit);
            return ResponseEntity.ok(buildSuccess(suggestions));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 统一响应成功
     */
    private Map<String, Object> buildSuccess(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }

    /**
     * 统一响应错误
     */
    private Map<String, Object> buildError(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 400);
        response.put("message", message);
        response.put("data", null);
        return response;
    }
}
