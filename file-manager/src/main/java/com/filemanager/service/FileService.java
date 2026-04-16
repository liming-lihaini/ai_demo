package com.filemanager.service;

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
import java.util.Optional;

/**
 * 文件业务逻辑层
 * 负责文件上传、查询、删除等业务逻辑
 */
@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private StorageService storageService;

    /**
     * 允许的文件扩展名
     */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf", "txt", "md",
            "jpg", "jpeg", "png", "gif", "bmp",
            "mp3", "wav",
            "mp4", "avi"
    );

    /**
     * 最大文件大小: 100MB
     */
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    /**
     * 上传文件
     * @param file 上传的文件
     * @param parentId 父目录ID
     * @param userId 用户ID
     * @return 上传后的文件信息
     */
    @Transactional
    public FileInfo uploadFile(MultipartFile file, Long parentId, Long userId) throws IOException {
        // 校验文件
        validateFile(file);

        // 计算 MD5
        String md5 = storageService.calculateMD5(file);

        // 检查 MD5 重复（同目录下）
        if (fileRepository.existsByMd5AndParentIdAndDeleted(md5, parentId, 0)) {
            throw new RuntimeException("该文件已存在（内容相同），无需重复上传");
        }

        // 保存文件到磁盘
        String relativePath = storageService.saveFile(file, userId);

        // 提取文件信息
        String originalFilename = file.getOriginalFilename();
        String name = originalFilename;
        String ext = getExtension(originalFilename);

        // 创建文件记录
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

        return fileRepository.save(fileInfo);
    }

    /**
     * 验证文件
     */
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

    /**
     * 获取文件扩展名
     */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 获取文件列表
     * @param parentId 父目录ID
     * @param userId 用户ID
     */
    public List<FileInfo> getFileList(Long parentId, Long userId) {
        if (parentId == null || parentId == 0) {
            // 根目录，返回用户根目录下的文件
            return fileRepository.findByUserIdAndParentIdAndDeleted(userId, null, 0);
        }
        return fileRepository.findByParentIdAndDeletedOrderByCreatedAtDesc(parentId, 0);
    }

    /**
     * 搜索文件
     * @param keyword 搜索关键字
     * @param userId 用户ID
     */
    public List<FileInfo> searchFiles(String keyword, Long userId) {
        return fileRepository.searchByName(keyword, userId);
    }

    /**
     * 删除文件（软删除）
     * @param id 文件ID
     */
    @Transactional
    public void deleteFile(Long id) {
        Optional<FileInfo> opt = fileRepository.findById(id);
        if (!opt.isPresent()) {
            throw new RuntimeException("文件不存在");
        }

        FileInfo fileInfo = opt.get();
        // 软删除
        fileInfo.setDeleted(1);
        fileInfo.setUpdatedAt(LocalDateTime.now());
        fileRepository.save(fileInfo);
    }

    /**
     * 批量删除文件（软删除）
     * @param ids 文件ID列表
     */
    @Transactional
    public void deleteFiles(List<Long> ids) {
        List<FileInfo> files = fileRepository.findByIdIn(ids);
        LocalDateTime now = LocalDateTime.now();

        for (FileInfo file : files) {
            file.setDeleted(1);
            file.setUpdatedAt(now);
        }
        fileRepository.saveAll(files);
    }

    /**
     * 恢复文件
     * @param id 文件ID
     */
    @Transactional
    public void restoreFile(Long id) {
        Optional<FileInfo> opt = fileRepository.findById(id);
        if (!opt.isPresent()) {
            throw new RuntimeException("文件不存在");
        }

        FileInfo fileInfo = opt.get();
        fileInfo.setDeleted(0);
        fileInfo.setUpdatedAt(LocalDateTime.now());
        fileRepository.save(fileInfo);
    }

    /**
     * 重命名文件
     * @param id 文件ID
     * @param newName 新名称
     */
    @Transactional
    public FileInfo renameFile(Long id, String newName) {
        Optional<FileInfo> opt = fileRepository.findById(id);
        if (!opt.isPresent()) {
            throw new RuntimeException("文件不存在");
        }

        FileInfo fileInfo = opt.get();

        // 校验名称
        if (newName == null || newName.isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }

        if (newName.length() > 50) {
            throw new RuntimeException("文件名不能超过50个字符");
        }

        // 检查同名文件是否存在
        if (fileRepository.existsByParentIdAndNameAndDeletedAndUserId(
                fileInfo.getParentId(), newName, 0, fileInfo.getUserId())) {
            throw new RuntimeException("当前目录下已存在同名文件");
        }

        fileInfo.setName(newName);
        fileInfo.setUpdatedAt(LocalDateTime.now());

        return fileRepository.save(fileInfo);
    }

    /**
     * 移动文件到其他目录
     * @param id 文件ID
     * @param newParentId 目标目录ID
     */
    @Transactional
    public FileInfo moveFile(Long id, Long newParentId) {
        Optional<FileInfo> opt = fileRepository.findById(id);
        if (!opt.isPresent()) {
            throw new RuntimeException("文件不存在");
        }

        FileInfo fileInfo = opt.get();
        fileInfo.setParentId(newParentId);
        fileInfo.setUpdatedAt(LocalDateTime.now());

        return fileRepository.save(fileInfo);
    }

    /**
     * 获取文件信息
     * @param id 文件ID
     */
    public Optional<FileInfo> getFileById(Long id) {
        return fileRepository.findById(id);
    }

    /**
     * 获取文件下载路径
     * @param id 文件ID
     */
    public String getDownloadPath(Long id) {
        Optional<FileInfo> opt = fileRepository.findById(id);
        if (!opt.isPresent()) {
            throw new RuntimeException("文件不存在");
        }

        FileInfo fileInfo = opt.get();

        // 检查文件是否被删除
        if (fileInfo.getDeleted() == 1) {
            throw new RuntimeException("文件已删除");
        }

        return storageService.getFullPath(fileInfo.getPath());
    }
}