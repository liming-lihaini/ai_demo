package com.filemanager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.filemanager.model.Directory;
import com.filemanager.model.FileInfo;
import com.filemanager.repository.DirectoryRepository;
import com.filemanager.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TrashService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private DirectoryService directoryService;

    @Autowired
    private StorageService storageService;

    private static final int EXPIRED_DAYS = 30;

    /**
     * 获取回收站列表
     */
    public List<Map<String, Object>> getTrashList(Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();

        // 查询已删除的文件
        QueryWrapper<FileInfo> fileWrapper = new QueryWrapper<>();
        fileWrapper.eq("user_id", userId)
                   .eq("deleted", 1)
                   .orderByDesc("delete_at");
        List<FileInfo> deletedFiles = fileRepository.selectList(fileWrapper);

        for (FileInfo file : deletedFiles) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", file.getId());
            item.put("name", file.getName());
            item.put("type", "file");
            item.put("deletedAt", file.getDeleteAt());
            result.add(item);
        }

        // 查询已删除的目录
        QueryWrapper<Directory> dirWrapper = new QueryWrapper<>();
        dirWrapper.eq("user_id", userId)
                  .eq("deleted", 1)
                  .orderByDesc("delete_at");
        List<Directory> deletedDirs = directoryRepository.selectList(dirWrapper);

        for (Directory dir : deletedDirs) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", dir.getId());
            item.put("name", dir.getName());
            item.put("type", "directory");
            item.put("deletedAt", dir.getDeleteAt());
            result.add(item);
        }

        return result;
    }

    /**
     * 统一恢复方法（自动判断类型）
     */
    @Transactional
    public void restore(Long id) {
        // 尝试作为文件恢复
        FileInfo fileInfo = fileRepository.selectById(id);
        if (fileInfo != null && fileInfo.getDeleted() == 1) {
            fileService.restoreFile(id);
            return;
        }

        // 尝试作为目录恢复
        Directory directory = directoryRepository.selectById(id);
        if (directory != null && directory.getDeleted() == 1) {
            directoryService.restoreDirectory(id);
            return;
        }

        throw new RuntimeException("回收站中不存在该项目");
    }

    /**
     * 统一永久删除方法（自动判断类型）
     */
    @Transactional
    public void permanentDelete(Long id) {
        // 尝试作为文件删除
        FileInfo fileInfo = fileRepository.selectById(id);
        if (fileInfo != null && fileInfo.getDeleted() == 1) {
            permanentDeleteFile(id);
            return;
        }

        // 尝试作为目录删除
        Directory directory = directoryRepository.selectById(id);
        if (directory != null && directory.getDeleted() == 1) {
            permanentDeleteDirectory(id);
            return;
        }

        throw new RuntimeException("回收站中不存在该项目");
    }

    /**
     * 永久删除文件
     */
    @Transactional
    public void permanentDeleteFile(Long id) {
        FileInfo fileInfo = fileRepository.selectById(id);
        if (fileInfo == null) {
            throw new RuntimeException("文件不存在");
        }

        // 删除物理文件
        if (fileInfo.getPath() != null) {
            try {
                storageService.deleteFile(fileInfo.getPath());
            } catch (Exception e) {
                // 忽略文件删除失败，可能已被手动删除
            }
        }

        // 物理删除数据库记录
        fileRepository.deleteById(id);
    }

    /**
     * 永久删除目录
     */
    @Transactional
    public void permanentDeleteDirectory(Long id) {
        Directory directory = directoryRepository.selectById(id);
        if (directory == null) {
            throw new RuntimeException("目录不存在");
        }

        // 递归删除子目录和文件
        deleteDirectoryRecursively(id);

        // 物理删除目录记录
        directoryRepository.deleteById(id);
    }

    private void deleteDirectoryRecursively(Long parentId) {
        // 删除子目录
        QueryWrapper<Directory> dirWrapper = new QueryWrapper<>();
        dirWrapper.eq("parent_id", parentId);
        List<Directory> children = directoryRepository.selectList(dirWrapper);
        for (Directory child : children) {
            deleteDirectoryRecursively(child.getId());
            directoryRepository.deleteById(child.getId());
        }

        // 删除目录中的文件
        QueryWrapper<FileInfo> fileWrapper = new QueryWrapper<>();
        fileWrapper.eq("parent_id", parentId);
        List<FileInfo> files = fileRepository.selectList(fileWrapper);
        for (FileInfo file : files) {
            if (file.getPath() != null) {
                try {
                    storageService.deleteFile(file.getPath());
                } catch (Exception e) {
                    // 忽略文件删除失败
                }
            }
            fileRepository.deleteById(file.getId());
        }
    }

    /**
     * 清空用户回收站
     */
    @Transactional
    public void clearTrash(Long userId) {
        // 永久删除所有已删除的文件
        QueryWrapper<FileInfo> fileWrapper = new QueryWrapper<>();
        fileWrapper.eq("user_id", userId)
                   .eq("deleted", 1);
        List<FileInfo> deletedFiles = fileRepository.selectList(fileWrapper);
        for (FileInfo file : deletedFiles) {
            permanentDeleteFile(file.getId());
        }

        // 永久删除所有已删除的目录
        QueryWrapper<Directory> dirWrapper = new QueryWrapper<>();
        dirWrapper.eq("user_id", userId)
                  .eq("deleted", 1);
        List<Directory> deletedDirs = directoryRepository.selectList(dirWrapper);
        for (Directory dir : deletedDirs) {
            permanentDeleteDirectory(dir.getId());
        }
    }

    /**
     * 清理过期项目（超过30天）
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanExpiredItems() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(EXPIRED_DAYS);

        // 查询并删除过期的文件
        QueryWrapper<FileInfo> fileWrapper = new QueryWrapper<>();
        fileWrapper.eq("deleted", 1)
                   .lt("delete_at", threshold);
        List<FileInfo> expiredFiles = fileRepository.selectList(fileWrapper);
        for (FileInfo file : expiredFiles) {
            try {
                permanentDeleteFile(file.getId());
            } catch (Exception e) {
                // 记录日志，继续清理其他文件
            }
        }

        // 查询并删除过期的目录
        QueryWrapper<Directory> dirWrapper = new QueryWrapper<>();
        dirWrapper.eq("deleted", 1)
                  .lt("delete_at", threshold);
        List<Directory> expiredDirs = directoryRepository.selectList(dirWrapper);
        for (Directory dir : expiredDirs) {
            try {
                permanentDeleteDirectory(dir.getId());
            } catch (Exception e) {
                // 记录日志，继续清理其他目录
            }
        }
    }
}