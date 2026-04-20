package com.filemanager.controller;

import com.filemanager.service.TrashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 回收站接口
 * 遵循 CLAUDE.md 架构约束：只做参数校验和响应格式化，不包含业务逻辑
 */
@RestController
@RequestMapping("/api/trash")
@CrossOrigin(origins = "*")
public class TrashController {

    @Autowired
    private TrashService trashService;

    /**
     * 获取回收站列表
     * GET /api/trash?userId=1
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getTrashList(
            @RequestParam(value = "userId", required = false, defaultValue = "1") Long userId) {
        try {
            List<Map<String, Object>> trashList = trashService.getTrashList(userId);
            return ResponseEntity.ok(buildSuccess(trashList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 恢复文件或目录
     * POST /api/trash/{id}/restore
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<Map<String, Object>> restore(@PathVariable Long id) {
        try {
            trashService.restore(id);
            return ResponseEntity.ok(buildSuccess(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 永久删除文件或目录
     * DELETE /api/trash/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> permanentDelete(@PathVariable Long id) {
        try {
            trashService.permanentDelete(id);
            return ResponseEntity.ok(buildSuccess(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 清空回收站
     * DELETE /api/trash/clear?userId=1
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearTrash(
            @RequestParam(value = "userId", required = false, defaultValue = "1") Long userId) {
        try {
            trashService.clearTrash(userId);
            return ResponseEntity.ok(buildSuccess(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
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