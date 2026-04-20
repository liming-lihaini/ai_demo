package com.filemanager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.filemanager.model.Directory;
import com.filemanager.model.FileInfo;
import com.filemanager.repository.DirectoryRepository;
import com.filemanager.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    /**
     * 搜索文件
     */
    public List<Map<String, Object>> searchFiles(String keyword, Long userId, String fileType,
                                                  Long startDate, Long endDate,
                                                  Long minSize, Long maxSize, int page, int size) {
        QueryWrapper<FileInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("deleted", 0);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("name", keyword);
        }

        if (fileType != null && !fileType.isEmpty()) {
            wrapper.eq("format", fileType);
        }

        if (startDate != null) {
            wrapper.ge("created_at", startDate);
        }

        if (endDate != null) {
            wrapper.le("created_at", endDate);
        }

        if (minSize != null) {
            wrapper.ge("size", minSize);
        }

        if (maxSize != null) {
            wrapper.le("size", maxSize);
        }

        wrapper.orderByDesc("created_at");

        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);

        List<FileInfo> files = fileRepository.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();

        for (FileInfo file : files) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", file.getId());
            item.put("name", file.getName());
            item.put("type", "file");
            item.put("format", file.getFormat());
            item.put("size", file.getSize());
            item.put("parentId", file.getParentId());
            item.put("createdAt", file.getCreatedAt());
            item.put("updatedAt", file.getUpdatedAt());
            result.add(item);
        }

        return result;
    }

    /**
     * 搜索目录
     */
    public List<Map<String, Object>> searchDirectories(String keyword, Long userId, int page, int size) {
        QueryWrapper<Directory> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("deleted", 0);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("name", keyword);
        }

        wrapper.orderByDesc("created_at");

        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);

        List<Directory> directories = directoryRepository.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Directory dir : directories) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", dir.getId());
            item.put("name", dir.getName());
            item.put("type", "directory");
            item.put("parentId", dir.getParentId());
            item.put("createdAt", dir.getCreatedAt());
            result.add(item);
        }

        return result;
    }

    /**
     * 混合搜索（文件+目录）
     */
    public Map<String, Object> searchAll(String keyword, Long userId, String fileType,
                                         Long startDate, Long endDate,
                                         Long minSize, Long maxSize, int page, int size) {
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> files = searchFiles(keyword, userId, fileType, startDate, endDate, minSize, maxSize, page, size);
        List<Map<String, Object>> directories = searchDirectories(keyword, userId, page, size);

        result.put("files", files);
        result.put("directories", directories);
        result.put("total", files.size() + directories.size());

        return result;
    }

    /**
     * 获取搜索建议（自动补全）
     */
    public List<String> getSuggestions(String keyword, Long userId, int limit) {
        List<String> suggestions = new ArrayList<>();

        // 从文件名获取建议
        QueryWrapper<FileInfo> fileWrapper = new QueryWrapper<>();
        fileWrapper.eq("user_id", userId).eq("deleted", 0)
                  .like("name", keyword)
                  .select("DISTINCT name")
                  .last("LIMIT " + limit);
        List<FileInfo> files = fileRepository.selectList(fileWrapper);
        suggestions.addAll(files.stream().map(FileInfo::getName).collect(Collectors.toList()));

        // 从目录名获取建议
        QueryWrapper<Directory> dirWrapper = new QueryWrapper<>();
        dirWrapper.eq("user_id", userId).eq("deleted", 0)
                  .like("name", keyword)
                  .select("DISTINCT name")
                  .last("LIMIT " + limit);
        List<Directory> directories = directoryRepository.selectList(dirWrapper);
        suggestions.addAll(directories.stream().map(Directory::getName).collect(Collectors.toList()));

        // 去重并返回
        return suggestions.stream().distinct().limit(limit).collect(Collectors.toList());
    }
}
