package com.filemanager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.filemanager.model.FileInfo;
import com.filemanager.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private StorageService storageService;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf", "txt", "md",
            "jpg", "jpeg", "png", "gif", "bmp",
            "mp3", "wav",
            "mp4", "avi"
    );

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    @Transactional
    public FileInfo uploadFile(MultipartFile file, Long parentId, Long userId) throws IOException {
        validateFile(file);

        String md5 = storageService.calculateMD5(file);

        QueryWrapper<FileInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("md5", md5)
               .eq("parent_id", parentId)
               .eq("deleted", 0);
        if (fileRepository.selectCount(wrapper) > 0) {
            throw new RuntimeException("该文件已存在（内容相同），无需重复上传");
        }

        String relativePath = storageService.saveFile(file, userId);

        String originalFilename = file.getOriginalFilename();
        String name = originalFilename;
        String ext = getExtension(originalFilename);

        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(name);
        fileInfo.setFormat(ext);
        fileInfo.setSize(file.getSize());
        fileInfo.setMd5(md5);
        fileInfo.setPath(relativePath);
        fileInfo.setParentId(parentId);
        fileInfo.setUserId(userId);
        fileInfo.setDeleted(0);
        fileInfo.setCreatedAt(LocalDateTime.now());
        fileInfo.setUpdatedAt(LocalDateTime.now());

        fileRepository.insert(fileInfo);
        return fileInfo;
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("文件大小不能超过100MB");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = getExtension(originalFilename).toLowerCase();

        if (ext.isEmpty()) {
            throw new IOException("无法识别文件类型");
        }

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IOException("不支持的文件格式: " + ext);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public List<FileInfo> getFileList(Long parentId, Long userId) {
        QueryWrapper<FileInfo> wrapper = new QueryWrapper<>();
        if (parentId == null || parentId == 0) {
            wrapper.eq("user_id", userId)
                   .isNull("parent_id")
                   .eq("deleted", 0);
        } else {
            wrapper.eq("parent_id", parentId)
                   .eq("deleted", 0)
                   .orderByDesc("created_at");
        }
        return fileRepository.selectList(wrapper);
    }

    public List<FileInfo> searchFiles(String keyword, Long userId) {
        QueryWrapper<FileInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0)
               .like("name", keyword)
               .eq("user_id", userId);
        return fileRepository.selectList(wrapper);
    }

    @Transactional
    public void deleteFile(Long id) {
        FileInfo fileInfo = fileRepository.selectById(id);
        if (fileInfo == null) {
            throw new RuntimeException("文件不存在");
        }

        fileInfo.setDeleted(1);
        fileInfo.setUpdatedAt(LocalDateTime.now());
        fileRepository.updateById(fileInfo);
    }

    @Transactional
    public void deleteFiles(List<Long> ids) {
        LocalDateTime now = LocalDateTime.now();
        for (Long id : ids) {
            FileInfo fileInfo = fileRepository.selectById(id);
            if (fileInfo != null) {
                fileInfo.setDeleted(1);
                fileInfo.setUpdatedAt(now);
                fileRepository.updateById(fileInfo);
            }
        }
    }

    @Transactional
    public void restoreFile(Long id) {
        FileInfo fileInfo = fileRepository.selectById(id);
        if (fileInfo == null) {
            throw new RuntimeException("文件不存在");
        }

        fileInfo.setDeleted(0);
        fileInfo.setUpdatedAt(LocalDateTime.now());
        fileRepository.updateById(fileInfo);
    }

    @Transactional
    public FileInfo renameFile(Long id, String newName) {
        FileInfo fileInfo = fileRepository.selectById(id);
        if (fileInfo == null) {
            throw new RuntimeException("文件不存在");
        }

        if (newName == null || newName.isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }

        if (newName.length() > 50) {
            throw new RuntimeException("文件名不能超过50个字符");
        }

        QueryWrapper<FileInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", fileInfo.getParentId())
               .eq("name", newName)
               .eq("deleted", 0)
               .eq("user_id", fileInfo.getUserId())
               .ne("id", id);
        if (fileRepository.selectCount(wrapper) > 0) {
            throw new RuntimeException("当前目录下已存在同名文件");
        }

        fileInfo.setName(newName);
        fileInfo.setUpdatedAt(LocalDateTime.now());
        fileRepository.updateById(fileInfo);
        return fileInfo;
    }

    @Transactional
    public FileInfo moveFile(Long id, Long newParentId) {
        FileInfo fileInfo = fileRepository.selectById(id);
        if (fileInfo == null) {
            throw new RuntimeException("文件不存在");
        }

        fileInfo.setParentId(newParentId);
        fileInfo.setUpdatedAt(LocalDateTime.now());
        fileRepository.updateById(fileInfo);
        return fileInfo;
    }

    public FileInfo getFileById(Long id) {
        return fileRepository.selectById(id);
    }

    public String getDownloadPath(Long id) {
        FileInfo fileInfo = fileRepository.selectById(id);
        if (fileInfo == null) {
            throw new RuntimeException("文件不存在");
        }

        if (fileInfo.getDeleted() == 1) {
            throw new RuntimeException("文件已删除");
        }

        return storageService.getFullPath(fileInfo.getPath());
    }

    @Transactional
    public FileInfo updateContent(Long id, String content) {
        FileInfo fileInfo = fileRepository.selectById(id);
        if (fileInfo == null) {
            throw new RuntimeException("文件不存在");
        }

        if (fileInfo.getDeleted() == 1) {
            throw new RuntimeException("文件已删除");
        }

        String filePath = storageService.getFullPath(fileInfo.getPath());
        try {
            java.nio.file.Files.writeString(java.nio.file.Paths.get(filePath), content,
                java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("保存文件失败: " + e.getMessage());
        }

        fileInfo.setSize((long) content.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        fileInfo.setUpdatedAt(LocalDateTime.now());
        fileRepository.updateById(fileInfo);
        return fileInfo;
    }
}