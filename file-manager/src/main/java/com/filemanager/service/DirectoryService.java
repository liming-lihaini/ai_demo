package com.filemanager.service;

import com.filemanager.model.Directory;
import com.filemanager.repository.DirectoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DirectoryService {

    @Autowired
    private DirectoryRepository directoryRepository;

    /**
     * 特殊字符禁止列表
     */
    private static final String FORBIDDEN_CHARS = "/\\:*?\"<>|";

    /**
     * 获取用户当前的根目录
     */
    public List<Directory> getRootDirectory(Long userId) {
        return directoryRepository.getRootDirectory(userId);
    }

    /**
     * 获取指定目录的子目录列表
     */
    public List<Directory> getChildDirectories(Long parentId) {
        return directoryRepository.findByParentIdAndDeletedOrderByCreatedAtDesc(parentId, 0);
    }

    /**
     * 创建目录
     */
    @Transactional
    public Directory createDirectory(String name, Long parentId, Long userId) {
        // 校验目录名
        validateDirectoryName(name);

        // 校验名称唯一性
        if (directoryRepository.existsByParentIdAndNameAndDeletedAndUserId(parentId, name, 0, userId)) {
            throw new RuntimeException("当前目录下已存在同名目录，请修改后重试");
        }

        Directory directory = new Directory();
        directory.setName(name);
        directory.setParentId(parentId);
        directory.setUserId(userId);
        directory.setDeleted(0);
        directory.setCreatedAt(LocalDateTime.now());
        directory.setUpdatedAt(LocalDateTime.now());

        return directoryRepository.save(directory);
    }

    /**
     * 更新目录（重命名或移动）
     */
    @Transactional
    public Directory updateDirectory(Long id, String newName, Long newParentId) {
        Optional<Directory> opt = directoryRepository.findById(id);
        if (!opt.isPresent()) {
            throw new RuntimeException("目录不存在");
        }

        Directory directory = opt.get();

        // 如果是重命名
        if (newName != null && !newName.isEmpty()) {
            validateDirectoryName(newName);
            directory.setName(newName);
        }

        // 如果是移动
        if (newParentId != null) {
            // 移动到自身或子目录不允许
            if (newParentId.equals(id)) {
                throw new RuntimeException("不能将目录移动到自身");
            }
            directory.setParentId(newParentId);
        }

        directory.setUpdatedAt(LocalDateTime.now());
        return directoryRepository.save(directory);
    }

    /**
     * 删除目录（软删除，移入回收站）
     */
    @Transactional
    public void deleteDirectory(Long id) {
        Optional<Directory> opt = directoryRepository.findById(id);
        if (!opt.isPresent()) {
            throw new RuntimeException("目录不存在");
        }

        Directory directory = opt.get();
        directory.setDeleted(1); // 软删除
        directory.setUpdatedAt(LocalDateTime.now());
        directoryRepository.save(directory);

        // 同时删除子目录（递归）
        List<Directory> children = directoryRepository.findByParentIdAndDeletedOrderByCreatedAtDesc(id, 0);
        for (Directory child : children) {
            deleteDirectory(child.getId());
        }
    }

    /**
     * 恢复目录（从回收站恢复）
     */
    @Transactional
    public void restoreDirectory(Long id) {
        Optional<Directory> opt = directoryRepository.findById(id);
        if (!opt.isPresent()) {
            throw new RuntimeException("目录不存在");
        }

        Directory directory = opt.get();
        directory.setDeleted(0); // 恢复
        directory.setUpdatedAt(LocalDateTime.now());
        directoryRepository.save(directory);
    }

    /**
     * 搜索目录
     */
    public List<Directory> searchDirectories(String keyword, Long userId) {
        return directoryRepository.searchByName(keyword, userId);
    }

    /**
     * 获取目录树
     */
    public List<Directory> getDirectoryTree(Long userId) {
        List<Directory> result = new ArrayList<>();
        List<Directory> roots = directoryRepository.getRootDirectory(userId);
        for (Directory root : roots) {
            buildTree(root, result);
        }
        return result;
    }

    private void buildTree(Directory directory, List<Directory> result) {
        result.add(directory);
        List<Directory> children = directoryRepository.findByParentIdAndDeletedOrderByCreatedAtDesc(directory.getId(), 0);
        for (Directory child : children) {
            buildTree(child, result);
        }
    }

    /**
     * 获取面包屑导航
     */
    public List<Directory> getBreadcrumb(Long id) {
        List<Directory> breadcrumb = new ArrayList<>();
        buildBreadcrumb(id, breadcrumb);
        return breadcrumb;
    }

    private void buildBreadcrumb(Long id, List<Directory> breadcrumb) {
        Optional<Directory> opt = directoryRepository.findById(id);
        if (opt.isPresent()) {
            Directory directory = opt.get();
            breadcrumb.add(0, directory);
            if (directory.getParentId() != null) {
                buildBreadcrumb(directory.getParentId(), breadcrumb);
            }
        }
    }

    /**
     * 校验目录名
     */
    private void validateDirectoryName(String name) {
        if (name == null || name.isEmpty()) {
            throw new RuntimeException("请输入目录名称");
        }
        if (name.length() > 50) {
            throw new RuntimeException("目录名称不能超过50个字符");
        }
        for (char c : FORBIDDEN_CHARS.toCharArray()) {
            if (name.indexOf(c) >= 0) {
                throw new RuntimeException("目录名不能包含以下字符: / \\ : * ? \" < > |");
            }
        }
    }
}