package com.filemanager.controller;

import com.filemanager.dto.DirectoryDTO;
import com.filemanager.model.Directory;
import com.filemanager.service.DirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/directory")
@CrossOrigin(origins = "*")
public class DirectoryController {

    @Autowired
    private DirectoryService directoryService;

    /**
     * 获取根目录列表
     */
    @GetMapping("/root")
    public ResponseEntity<Map<String, Object>> getRootDirectories(@RequestParam(defaultValue = "1") Long userId) {
        List<Directory> directories = directoryService.getRootDirectory(userId);
        return ResponseEntity.ok(buildSuccess(directories));
    }

    /**
     * 获取子目录列表
     */
    @GetMapping("/children/{parentId}")
    public ResponseEntity<Map<String, Object>> getChildDirectories(@PathVariable Long parentId) {
        List<Directory> directories = directoryService.getChildDirectories(parentId);
        return ResponseEntity.ok(buildSuccess(directories));
    }

    /**
     * 创建目录
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createDirectory(@RequestBody DirectoryDTO dto, @RequestParam(defaultValue = "1") Long userId) {
        try {
            dto.setUserId(userId);
            Directory directory = directoryService.createDirectory(dto.getName(), dto.getParentId(), userId);
            return ResponseEntity.ok(buildSuccess(directory));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 更新目录
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateDirectory(@RequestBody DirectoryDTO dto) {
        try {
            Directory directory = directoryService.updateDirectory(dto.getId(), dto.getName(), dto.getParentId());
            return ResponseEntity.ok(buildSuccess(directory));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 删除目录
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteDirectory(@PathVariable Long id) {
        try {
            directoryService.deleteDirectory(id);
            return ResponseEntity.ok(buildSuccess(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 恢复目录
     */
    @PostMapping("/restore/{id}")
    public ResponseEntity<Map<String, Object>> restoreDirectory(@PathVariable Long id) {
        try {
            directoryService.restoreDirectory(id);
            return ResponseEntity.ok(buildSuccess(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildError(e.getMessage()));
        }
    }

    /**
     * 搜索目录
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchDirectories(@RequestParam String keyword, @RequestParam(defaultValue = "1") Long userId) {
        List<Directory> directories = directoryService.searchDirectories(keyword, userId);
        return ResponseEntity.ok(buildSuccess(directories));
    }

    /**
     * 获取目录树
     */
    @GetMapping("/tree")
    public ResponseEntity<Map<String, Object>> getDirectoryTree(@RequestParam(defaultValue = "1") Long userId) {
        List<Directory> tree = directoryService.getDirectoryTree(userId);
        return ResponseEntity.ok(buildSuccess(tree));
    }

    /**
     * 获取面包屑导航
     */
    @GetMapping("/breadcrumb/{id}")
    public ResponseEntity<Map<String, Object>> getBreadcrumb(@PathVariable Long id) {
        List<Directory> breadcrumb = directoryService.getBreadcrumb(id);
        return ResponseEntity.ok(buildSuccess(breadcrumb));
    }

    private Map<String, Object> buildSuccess(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "success");
        result.put("data", data);
        return result;
    }

    private Map<String, Object> buildError(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 1);
        result.put("message", message);
        return result;
    }
}