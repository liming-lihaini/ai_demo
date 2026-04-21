package com.filemanager.service;

import com.filemanager.repository.DirectoryRepository;
import com.filemanager.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${storage.path:./storage/files}")
    private String storagePath;

    /**
     * 使用 FTS5 搜索文件
     */
    public List<Map<String, Object>> searchFiles(String keyword, Long userId, String fileType,
                                                   Long startDate, Long endDate,
                                                   Long minSize, Long maxSize, int page, int size) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (keyword == null || keyword.isEmpty()) {
            // 无关键词时返回所有文件
            return searchFilesByConditions(userId, fileType, startDate, endDate, minSize, maxSize, page, size);
        }

        try {
            // 使用 FTS5 全文搜索
            String ftsQuery = keyword.replace("\"", "\"\"");
            String sql = """
                SELECT f.id, f.name, f.format, f.size, f.parent_id as parentId,
                       f.created_at as createdAt, f.updated_at as updatedAt,
                       file_fts.rank
                FROM file_fts
                JOIN files f ON file_fts.rowid = f.id
                WHERE file_fts MATCH ? AND f.user_id = ? AND f.deleted = 0
                ORDER BY file_fts.rank
                LIMIT ? OFFSET ?
                """;

            int offset = (page - 1) * size;
            List<Map<String, Object>> ftsResults = jdbcTemplate.queryForList(sql,
                "\"" + ftsQuery + "\"*", userId, size, offset);

            // 额外条件过滤（文件类型、日期、大小）
            for (Map<String, Object> item : ftsResults) {
                if (fileType != null && !fileType.isEmpty()) {
                    String format = (String) item.get("format");
                    if (!isMatchFileType(format, fileType)) continue;
                }

                Long createdAt = null;
                if (item.get("createdAt") instanceof java.time.LocalDateTime) {
                    createdAt = ((java.time.LocalDateTime) item.get("createdAt"))
                        .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                }

                if (startDate != null && createdAt != null && createdAt < startDate) continue;
                if (endDate != null && createdAt != null && createdAt > endDate) continue;

                Long sizeVal = (Long) item.get("size");
                if (minSize != null && sizeVal != null && sizeVal < minSize) continue;
                if (maxSize != null && sizeVal != null && sizeVal > maxSize) continue;

                item.put("type", "file");
                result.add(item);
            }

        } catch (Exception e) {
            // FTS 查询失败时回退到 LIKE 查询
            return searchFilesFallback(keyword, userId, fileType, startDate, endDate, minSize, maxSize, page, size);
        }

        return result;
    }

    /**
     * 无关键词时的条件查询
     */
    private List<Map<String, Object>> searchFilesByConditions(Long userId, String fileType,
                                                               Long startDate, Long endDate,
                                                               Long minSize, Long maxSize, int page, int size) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, name, format, size, parent_id as parentId, ");
        sql.append("created_at as createdAt, updated_at as updatedAt ");
        sql.append("FROM files WHERE user_id = ? AND deleted = 0");

        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (fileType != null && !fileType.isEmpty()) {
            String[] types = fileType.split(",");
            sql.append(" AND format IN (");
            for (int i = 0; i < types.length; i++) {
                sql.append("?");
                if (i < types.length - 1) sql.append(",");
                params.add(types[i].trim());
            }
            sql.append(")");
        }

        if (startDate != null) {
            sql.append(" AND created_at >= ?");
            params.add(java.time.Instant.ofEpochMilli(startDate).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }

        if (endDate != null) {
            sql.append(" AND created_at <= ?");
            params.add(java.time.Instant.ofEpochMilli(endDate).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }

        if (minSize != null) {
            sql.append(" AND size >= ?");
            params.add(minSize);
        }

        if (maxSize != null) {
            sql.append(" AND size <= ?");
            params.add(maxSize);
        }

        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        int offset = (page - 1) * size;
        params.add(size);
        params.add(offset);

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString(), params.toArray());

        for (Map<String, Object> item : results) {
            item.put("type", "file");
        }

        return results;
    }

    /**
     * FTS 查询失败时的回退方案
     */
    private List<Map<String, Object>> searchFilesFallback(String keyword, Long userId, String fileType,
                                                           Long startDate, Long endDate,
                                                           Long minSize, Long maxSize, int page, int size) {
        return searchFilesByConditions(userId, fileType, startDate, endDate, minSize, maxSize, page, size);
    }

    private boolean isMatchFileType(String format, String fileType) {
        if (format == null || fileType == null) return true;
        String[] types = fileType.split(",");
        for (String type : types) {
            if (type.trim().equalsIgnoreCase(format)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 使用 FTS5 搜索目录
     */
    public List<Map<String, Object>> searchDirectories(String keyword, Long userId, int page, int size) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (keyword == null || keyword.isEmpty()) {
            return searchAllDirectories(userId, page, size);
        }

        try {
            String ftsQuery = keyword.replace("\"", "\"\"");
            String sql = """
                SELECT d.id, d.name, d.parent_id as parentId,
                       d.created_at as createdAt,
                       directory_fts.rank
                FROM directory_fts
                JOIN directories d ON directory_fts.rowid = d.id
                WHERE directory_fts MATCH ? AND d.user_id = ? AND d.deleted = 0
                ORDER BY directory_fts.rank
                LIMIT ? OFFSET ?
                """;

            int offset = (page - 1) * size;
            List<Map<String, Object>> ftsResults = jdbcTemplate.queryForList(sql,
                "\"" + ftsQuery + "\"*", userId, size, offset);

            for (Map<String, Object> item : ftsResults) {
                item.put("type", "directory");
                result.add(item);
            }

        } catch (Exception e) {
            return searchDirectoriesFallback(keyword, userId, page, size);
        }

        return result;
    }

    private List<Map<String, Object>> searchAllDirectories(Long userId, int page, int size) {
        String sql = """
            SELECT id, name, parent_id as parentId, created_at as createdAt
            FROM directories WHERE user_id = ? AND deleted = 0
            ORDER BY created_at DESC LIMIT ? OFFSET ?
            """;
        int offset = (page - 1) * size;
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, userId, size, offset);

        for (Map<String, Object> item : results) {
            item.put("type", "directory");
        }
        return results;
    }

    private List<Map<String, Object>> searchDirectoriesFallback(String keyword, Long userId, int page, int size) {
        return searchAllDirectories(userId, page, size);
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
        if (keyword == null || keyword.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> suggestions = new ArrayList<>();

        try {
            // 从 FTS 获取文件建议
            String sql = """
                SELECT DISTINCT f.name FROM files f
                JOIN file_fts ON file_fts.rowid = f.id
                WHERE file_fts MATCH ? AND f.user_id = ? AND f.deleted = 0
                LIMIT ?
                """;
            List<String> fileSuggestions = jdbcTemplate.queryForList(sql, String.class,
                "\"" + keyword.replace("\"", "\"\"") + "\"*", userId, limit);
            suggestions.addAll(fileSuggestions);

            // 从 FTS 获取目录建议
            String dirSql = """
                SELECT DISTINCT d.name FROM directories d
                JOIN directory_fts ON directory_fts.rowid = d.id
                WHERE directory_fts MATCH ? AND d.user_id = ? AND d.deleted = 0
                LIMIT ?
                """;
            List<String> dirSuggestions = jdbcTemplate.queryForList(dirSql, String.class,
                "\"" + keyword.replace("\"", "\"\"") + "\"*", userId, limit);
            suggestions.addAll(dirSuggestions);

        } catch (Exception e) {
            // 回退到 LIKE 查询
            return getSuggestionsFallback(keyword, userId, limit);
        }

        return suggestions.stream().distinct().limit(limit).collect(Collectors.toList());
    }

    private List<String> getSuggestionsFallback(String keyword, Long userId, int limit) {
        List<String> suggestions = new ArrayList<>();

        String fileSql = "SELECT DISTINCT name FROM files WHERE name LIKE ? AND user_id = ? AND deleted = 0 LIMIT ?";
        List<String> files = jdbcTemplate.queryForList(fileSql, String.class, "%" + keyword + "%", userId, limit);
        suggestions.addAll(files);

        String dirSql = "SELECT DISTINCT name FROM directories WHERE name LIKE ? AND user_id = ? AND deleted = 0 LIMIT ?";
        List<String> dirs = jdbcTemplate.queryForList(dirSql, String.class, "%" + keyword + "%", userId, limit);
        suggestions.addAll(dirs);

        return suggestions.stream().distinct().limit(limit).collect(Collectors.toList());
    }
}
