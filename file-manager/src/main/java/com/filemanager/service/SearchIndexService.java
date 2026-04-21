package com.filemanager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.filemanager.model.Directory;
import com.filemanager.model.FileInfo;
import com.filemanager.repository.DirectoryRepository;
import com.filemanager.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 搜索索引同步服务
 * 负责定时将文件和目录数据同步到 FTS5 索引表
 */
@Service
public class SearchIndexService {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexService.class);

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StorageService storageService;

    /**
     * 同步文件索引
     * 每5分钟执行一次，启动延迟10秒
     */
    @Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 10000)
    @Transactional
    public void syncFileIndex() {
        log.info("开始同步文件索引...");

        try {
            // 清理现有 FTS 索引
            jdbcTemplate.execute("DELETE FROM file_fts");

            // 重新同步所有未删除的文件
            QueryWrapper<FileInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("deleted", 0);
            List<FileInfo> files = fileRepository.selectList(wrapper);

            for (FileInfo file : files) {
                // 获取文件内容（如果是文本文件）
                String content = getFileContent(file);

                // 插入 FTS 索引
                jdbcTemplate.update(
                    "INSERT INTO file_fts(rowid, name, content) VALUES (?, ?, ?)",
                    file.getId(),
                    file.getName(),
                    content != null ? content : ""
                );
            }

            log.info("文件索引同步完成，共同步 {} 条", files.size());
        } catch (Exception e) {
            log.error("文件索引同步失败", e);
        }
    }

    /**
     * 同步目录索引
     * 每5分钟执行一次，启动延迟10秒
     */
    @Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 10000)
    @Transactional
    public void syncDirectoryIndex() {
        log.info("开始同步目录索引...");

        try {
            // 清理现有 FTS 索引
            jdbcTemplate.execute("DELETE FROM directory_fts");

            // 重新同步所有未删除的目录
            QueryWrapper<Directory> wrapper = new QueryWrapper<>();
            wrapper.eq("deleted", 0);
            List<Directory> directories = directoryRepository.selectList(wrapper);

            for (Directory dir : directories) {
                // 插入 FTS 索引
                jdbcTemplate.update(
                    "INSERT INTO directory_fts(rowid, name) VALUES (?, ?)",
                    dir.getId(),
                    dir.getName()
                );
            }

            log.info("目录索引同步完成，共同步 {} 条", directories.size());
        } catch (Exception e) {
            log.error("目录索引同步失败", e);
        }
    }

    /**
     * 获取文件内容（仅文本文件）
     */
    private String getFileContent(FileInfo file) {
        String format = file.getFormat().toLowerCase();

        // 仅支持文本文件内容检索
        if (!isTextFile(format)) {
            return null;
        }

        // 读取实际文件内容
        try {
            String fullPath = storageService.getFullPath(file.getPath());
            java.nio.file.Path path = java.nio.file.Paths.get(fullPath);
            if (java.nio.file.Files.exists(path)) {
                // 限制内容长度，避免索引过大
                String content = java.nio.file.Files.readString(path, java.nio.charset.StandardCharsets.UTF_8);
                if (content.length() > 10000) {
                    content = content.substring(0, 10000);
                }
                return content;
            }
        } catch (Exception e) {
            log.warn("读取文件内容失败: {}", file.getPath(), e);
        }
        return null;
    }

    /**
     * 判断是否为文本文件
     */
    private boolean isTextFile(String format) {
        return format.equals("txt") ||
               format.equals("md") ||
               format.equals("json") ||
               format.equals("xml") ||
               format.equals("html") ||
               format.equals("css") ||
               format.equals("js") ||
               format.equals("java") ||
               format.equals("py") ||
               format.equals("log");
    }
}
