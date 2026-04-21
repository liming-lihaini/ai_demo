package com.filemanager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.filemanager.model.Directory;
import com.filemanager.repository.DirectoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DirectoryService {

    @Autowired
    private DirectoryRepository directoryRepository;

    private static final String FORBIDDEN_CHARS = "/\\:*?\"<>|";

    public List<Directory> getRootDirectory(Long userId) {
        QueryWrapper<Directory> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .isNull("parent_id")
               .eq("deleted", 0);
        return directoryRepository.selectList(wrapper);
    }

    public List<Directory> getChildDirectories(Long parentId) {
        QueryWrapper<Directory> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentId)
               .eq("deleted", 0)
               .orderByDesc("created_at");
        return directoryRepository.selectList(wrapper);
    }

    @Transactional
    public Directory createDirectory(String name, Long parentId, Long userId) {
        validateDirectoryName(name);

        QueryWrapper<Directory> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentId)
               .eq("name", name)
               .eq("deleted", 0)
               .eq("user_id", userId);
        if (directoryRepository.selectCount(wrapper) > 0) {
            throw new RuntimeException("当前目录下已存在同名目录，请修改后重试");
        }

        Directory directory = new Directory();
        directory.setName(name);
        directory.setParentId(parentId);
        directory.setUserId(userId);
        directory.setDeleted(0);
        directory.setCreatedAt(LocalDateTime.now());
        directory.setUpdatedAt(LocalDateTime.now());

        directoryRepository.insert(directory);
        return directory;
    }

    @Transactional
    public Directory updateDirectory(Long id, String newName, Long newParentId) {
        Directory directory = directoryRepository.selectById(id);
        if (directory == null) {
            throw new RuntimeException("目录不存在");
        }

        if (newName != null && !newName.isEmpty()) {
            validateDirectoryName(newName);
            directory.setName(newName);
        }

        if (newParentId != null) {
            if (newParentId.equals(id)) {
                throw new RuntimeException("不能将目录移动到自身");
            }
            directory.setParentId(newParentId);
        }

        directory.setUpdatedAt(LocalDateTime.now());
        directoryRepository.updateById(directory);
        return directory;
    }

    @Transactional
    public void deleteDirectory(Long id) {
        Directory directory = directoryRepository.selectById(id);
        if (directory == null) {
            throw new RuntimeException("目录不存在");
        }

        // 使用原生 SQL 更新，绕过逻辑删除
        LocalDateTime now = LocalDateTime.now();
        directoryRepository.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Directory>()
            .eq("id", id)
            .set("deleted", 1)
            .set("delete_at", now)
            .set("updated_at", now));

        List<Directory> children = getChildDirectories(id);
        for (Directory child : children) {
            deleteDirectory(child.getId());
        }
    }

    @Transactional
    public void restoreDirectory(Long id) {
        Directory directory = directoryRepository.selectById(id);
        if (directory == null) {
            throw new RuntimeException("目录不存在");
        }

        directory.setDeleted(0);
        directory.setDeleteAt(null);
        directory.setUpdatedAt(LocalDateTime.now());
        directoryRepository.updateById(directory);
    }

    public List<Directory> searchDirectories(String keyword, Long userId) {
        QueryWrapper<Directory> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0)
               .like("name", keyword)
               .eq("user_id", userId);
        return directoryRepository.selectList(wrapper);
    }

    public List<Directory> getDirectoryTree(Long userId) {
        List<Directory> allDirs = new ArrayList<>();
        List<Directory> roots = getRootDirectory(userId);
        for (Directory root : roots) {
            buildTree(root, allDirs);
        }
        // 转换为树结构
        return buildTreeStructure(allDirs);
    }

    private List<Directory> buildTreeStructure(List<Directory> allDirs) {
        // 按ID分组
        Map<Long, Directory> dirMap = allDirs.stream()
                .collect(Collectors.toMap(Directory::getId, d -> d));

        List<Directory> result = new ArrayList<>();
        for (Directory dir : allDirs) {
            if (dir.getParentId() == null) {
                result.add(dir);
            } else {
                Directory parent = dirMap.get(dir.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dir);
                }
            }
        }
        return result;
    }

    private void buildTree(Directory directory, List<Directory> result) {
        result.add(directory);
        List<Directory> children = getChildDirectories(directory.getId());
        for (Directory child : children) {
            buildTree(child, result);
        }
    }

    public List<Directory> getBreadcrumb(Long id) {
        List<Directory> breadcrumb = new ArrayList<>();
        buildBreadcrumb(id, breadcrumb);
        return breadcrumb;
    }

    private void buildBreadcrumb(Long id, List<Directory> breadcrumb) {
        Directory directory = directoryRepository.selectById(id);
        if (directory != null) {
            breadcrumb.add(0, directory);
            if (directory.getParentId() != null) {
                buildBreadcrumb(directory.getParentId(), breadcrumb);
            }
        }
    }

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