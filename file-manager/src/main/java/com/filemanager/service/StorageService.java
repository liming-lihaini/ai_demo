package com.filemanager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * 文件存储服务
 * 负责文件物理存储、读取、删除
 */
@Service
public class StorageService {

    @Value("${storage.path:./storage/files}")
    private String storagePath;

    @Value("${storage.temp-path:./storage/temp}")
    private String tempPath;

    /**
     * 保存文件到存储目录
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 存储后的相对路径
     */
    public String saveFile(MultipartFile file, Long userId) throws IOException {
        // 创建用户目录: storagePath/year/month/day/
        LocalDate today = LocalDate.now();
        String datePath = String.format("%d/%02d/%02d/", today.getYear(), today.getMonthValue(), today.getDayOfMonth());

        // 确保目录存在
        Path dirPath = Paths.get(storagePath, userId.toString(), datePath);
        Files.createDirectories(dirPath);

        // 生成文件名（使用原始名称，但需处理重名）
        String originalFilename = file.getOriginalFilename();
        String fileName = generateUniqueFileName(originalFilename, dirPath);

        // 保存文件
        Path filePath = dirPath.resolve(fileName);
        file.transferTo(filePath.toFile());

        // 返回相对路径（用于数据库存储）
        return userId + "/" + datePath + fileName;
    }

    /**
     * 生成唯一文件名
     */
    private String generateUniqueFileName(String originalFilename, Path dirPath) throws IOException {
        String baseName;
        String extension;

        if (originalFilename != null && originalFilename.contains(".")) {
            int lastDot = originalFilename.lastIndexOf(".");
            baseName = originalFilename.substring(0, lastDot);
            extension = originalFilename.substring(lastDot);
        } else {
            baseName = originalFilename != null ? originalFilename : "file";
            extension = "";
        }

        // 检查文件是否存在
        Path targetPath = dirPath.resolve(originalFilename);
        if (!Files.exists(targetPath)) {
            return originalFilename;
        }

        // 如果存在，添加时间戳
        long timestamp = System.currentTimeMillis();
        return baseName + "_" + timestamp + extension;
    }

    /**
     * 计算文件的 MD5 值
     */
    public String calculateMD5(MultipartFile file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = file.getBytes();
            md.update(bytes);
            return HexFormat.of().formatHex(md.digest());
        } catch (Exception e) {
            throw new IOException("计算MD5失败", e);
        }
    }

    /**
     * 计算已存在文件的 MD5 值
     */
    public String calculateMD5FromFile(String relativePath) throws IOException {
        Path fullPath = Paths.get(storagePath, relativePath);
        if (!Files.exists(fullPath)) {
            throw new IOException("文件不存在: " + relativePath);
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = Files.readAllBytes(fullPath);
            md.update(bytes);
            return HexFormat.of().formatHex(md.digest());
        } catch (Exception e) {
            throw new IOException("计算文件MD5失败", e);
        }
    }

    /**
     * 计算字符串内容的 MD5 值
     */
    public String calculateMD5FromContent(String content) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            md.update(bytes);
            return HexFormat.of().formatHex(md.digest());
        } catch (Exception e) {
            throw new IOException("计算内容MD5失败", e);
        }
    }

    /**
     * 保存 Markdown 文件内容
     * @param content 文件内容
     * @param userId 用户ID
     * @param baseName 不带扩展名的文件名
     * @return 存储后的相对路径
     */
    public String saveMdFile(String content, Long userId, String baseName) throws IOException {
        // 创建用户目录: storagePath/year/month/day/
        LocalDate today = LocalDate.now();
        String datePath = String.format("%d/%02d/%02d/", today.getYear(), today.getMonthValue(), today.getDayOfMonth());

        // 确保目录存在
        Path dirPath = Paths.get(storagePath, userId.toString(), datePath);
        Files.createDirectories(dirPath);

        // 生成文件名
        String fileName = generateUniqueFileName(baseName + ".md", dirPath);

        // 保存文件
        Path filePath = dirPath.resolve(fileName);
        Files.writeString(filePath, content, java.nio.charset.StandardCharsets.UTF_8);

        // 返回相对路径（用于数据库存储）
        return userId + "/" + datePath + fileName;
    }

    /**
     * 删除文件
     * @param relativePath 相对存储路径
     */
    public void deleteFile(String relativePath) throws IOException {
        if (relativePath == null || relativePath.isEmpty()) {
            return;
        }

        Path fullPath = Paths.get(storagePath, relativePath);
        if (Files.exists(fullPath)) {
            Files.delete(fullPath);
        }
    }

    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return false;
        }
        Path fullPath = Paths.get(storagePath, relativePath);
        return Files.exists(fullPath);
    }

    /**
     * 获取文件的完整路径
     */
    public String getFullPath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return storagePath;
        }
        return Paths.get(storagePath, relativePath).toString();
    }

    /**
     * 获取存储目录路径
     */
    public String getStoragePath() {
        return storagePath;
    }

    /**
     * 初始化存储目录
     */
    public void initStorageDirectory() throws IOException {
        Path storageDir = Paths.get(storagePath);
        Path tempDir = Paths.get(tempPath);

        Files.createDirectories(storageDir);
        Files.createDirectories(tempDir);
    }
}