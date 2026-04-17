package com.filemanager.service;

import com.filemanager.dto.PreviewInfo;
import com.filemanager.model.FileInfo;
import com.filemanager.repository.FileRepository;
import com.filemanager.util.OfficeConverter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预览服务
 * 负责提供文件预览信息、内容、缩略图等
 */
@Service
public class PreviewService {

    private static final Logger logger = LoggerFactory.getLogger(PreviewService.class);

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private StorageService storageService;

    @Value("${storage.path:./storage/files}")
    private String storagePath;

    /**
     * 支持直接预览的格式（无需转换）
     */
    private static final List<String> DIRECT_PREVIEW_FORMATS = Arrays.asList(
            "pdf", "txt", "md",
            "jpg", "jpeg", "png", "gif", "bmp",
            "mp3", "wav", "mp4", "avi"
    );

    /**
     * 图片格式
     */
    private static final List<String> IMAGE_FORMATS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp"
    );

    /**
     * 音频格式
     */
    private static final List<String> AUDIO_FORMATS = Arrays.asList(
            "mp3", "wav"
    );

    /**
     * 视频格式
     */
    private static final List<String> VIDEO_FORMATS = Arrays.asList(
            "mp4", "avi"
    );

    /**
     * 文本格式
     */
    private static final List<String> TEXT_FORMATS = Arrays.asList(
            "txt", "md"
    );

    /**
     * Office 格式
     */
    private static final List<String> OFFICE_FORMATS = Arrays.asList(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx"
    );

    /**
     * 大文件阈值: 20MB
     */
    private static final long LARGE_FILE_THRESHOLD = 20 * 1024 * 1024;

    /**
     * 获取预览信息
     *
     * @param fileId 文件ID
     * @return 预览信息
     */
    @Transactional(readOnly = true)
    public PreviewInfo getPreviewInfo(Long fileId) {
        FileInfo fileInfo = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));

        // 检查是否被删除
        if (fileInfo.getDeleted() == 1) {
            throw new RuntimeException("文件已删除");
        }

        PreviewInfo info = new PreviewInfo();
        info.setFileId(fileInfo.getId());
        info.setFileName(fileInfo.getName());
        info.setFormat(fileInfo.getFormat());
        info.setSize(fileInfo.getSize());

        // 确定预览类型
        String format = fileInfo.getFormat().toLowerCase();
        String previewType = getPreviewType(format);
        info.setPreviewType(previewType);

        // 检查是否需要转换
        boolean needConvert = OFFICE_FORMATS.contains(format) && previewType.equals("text");
        info.setNeedConvert(needConvert);

        return info;
    }

    /**
     * 获取预览类型
     */
    private String getPreviewType(String format) {
        if (DIRECT_PREVIEW_FORMATS.contains(format)) {
            if (IMAGE_FORMATS.contains(format)) {
                return "image";
            } else if (AUDIO_FORMATS.contains(format)) {
                return "audio";
            } else if (VIDEO_FORMATS.contains(format)) {
                return "video";
            } else if (TEXT_FORMATS.contains(format)) {
                return "text";
            } else if (format.equals("pdf")) {
                return "pdf";
            }
        }
        if (OFFICE_FORMATS.contains(format)) {
            return "text";
        }
        return "unsupported";
    }

    /**
     * 获取文件内容流（用于直接预览）
     *
     * @param fileId 文件ID
     * @return 文件内容
     */
    @Transactional(readOnly = true)
    public Resource getPreviewContent(Long fileId) throws IOException {
        FileInfo fileInfo = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));

        if (fileInfo.getDeleted() == 1) {
            throw new RuntimeException("文件已删除");
        }

        Path fullPath = Paths.get(storagePath, fileInfo.getPath());
        Resource resource = new UrlResource(fullPath.toUri());

        if (!resource.exists()) {
            throw new IOException("文件不存在: " + fileInfo.getPath());
        }

        return resource;
    }

    /**
     * 获取 PDF 预览内容（PDF 直接返回，Office 转换为文本）
     *
     * @param fileId 文件ID
     * @return 转换后的内容
     */
    @Transactional(readOnly = true)
    public byte[] getPdfPreview(Long fileId) throws IOException {
        FileInfo fileInfo = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));

        if (fileInfo.getDeleted() == 1) {
            throw new RuntimeException("文件已删除");
        }

        String format = fileInfo.getFormat().toLowerCase();

        // PDF 直接返回
        if (format.equals("pdf")) {
            Path fullPath = Paths.get(storagePath, fileInfo.getPath());
            return Files.readAllBytes(fullPath);
        }

        // Office 格式转换为文本
        if (OFFICE_FORMATS.contains(format)) {
            Path fullPath = Paths.get(storagePath, fileInfo.getPath());
            try (InputStream inputStream = Files.newInputStream(fullPath)) {
                return OfficeConverter.convertToText(inputStream, format).getBytes(StandardCharsets.UTF_8);
            }
        }

        throw new IOException("不支持的格式: " + format);
    }

    /**
     * 获取文本内容（TXT/MD 文本）
     *
     * @param fileId 文件ID
     * @return 文本内容
     */
    @Transactional(readOnly = true)
    public String getTextPreview(Long fileId) throws IOException {
        FileInfo fileInfo = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));

        if (fileInfo.getDeleted() == 1) {
            throw new RuntimeException("文件已删除");
        }

        String format = fileInfo.getFormat().toLowerCase();

        if (!TEXT_FORMATS.contains(format)) {
            throw new IOException("不支持的格式: " + format);
        }

        Path fullPath = Paths.get(storagePath, fileInfo.getPath());
        return new String(Files.readAllBytes(fullPath), StandardCharsets.UTF_8);
    }

    /**
     * 获取 HTML 预览内容（用于 Office 文档预览）
     *
     * @param fileId 文件ID
     * @return HTML 内容
     */
    @Transactional(readOnly = true)
    public String getHtmlPreview(Long fileId) throws IOException {
        FileInfo fileInfo = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));

        if (fileInfo.getDeleted() == 1) {
            throw new RuntimeException("文件已删除");
        }

        String format = fileInfo.getFormat().toLowerCase();

        // Office 文档转换为 HTML
        if (OFFICE_FORMATS.contains(format)) {
            Path fullPath = Paths.get(storagePath, fileInfo.getPath());
            try (InputStream inputStream = Files.newInputStream(fullPath)) {
                return OfficeConverter.convertToHtml(inputStream, format);
            }
        }

        // PDF 使用 PDFBox 提取文本
        if (format.equals("pdf")) {
            Path fullPath = Paths.get(storagePath, fileInfo.getPath());
            try (PDDocument document = PDDocument.load(fullPath.toFile())) {
                PDFTextStripper stripper = new PDFTextStripper();
                return textToHtml(stripper.getText(document));
            }
        }

        throw new IOException("不支持的格式: " + format);
    }

    /**
     * 简单文本转 HTML（带基本样式）
     */
    private String textToHtml(String text) {
        String escaped = text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");

        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">" +
                "<style>body{font-family:Arial,sans-serif;padding:20px;line-height:1.6;}</style>" +
                "</head><body>" + escaped + "</body></html>";
    }

    /**
     * 获取缩略图（图片/视频）
     *
     * @param fileId 文件ID
     * @return 缩略图路径
     */
    @Transactional(readOnly = true)
    public Resource getThumbnail(Long fileId) throws IOException {
        FileInfo fileInfo = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));

        if (fileInfo.getDeleted() == 1) {
            throw new RuntimeException("文件已删除");
        }

        String format = fileInfo.getFormat().toLowerCase();

        // 图片直接返回
        if (IMAGE_FORMATS.contains(format)) {
            Path fullPath = Paths.get(storagePath, fileInfo.getPath());
            return new UrlResource(fullPath.toUri());
        }

        throw new IOException("不支持生成缩略图: " + format);
    }

    /**
     * 判断文件是否为大型文件
     */
    public boolean isLargeFile(Long fileId) {
        FileInfo fileInfo = fileRepository.findById(fileId)
                .orElse(null);
        return fileInfo != null && fileInfo.getSize() > LARGE_FILE_THRESHOLD;
    }

    /**
     * 获取文件大小
     */
    public long getFileSize(Long fileId) {
        FileInfo fileInfo = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        return fileInfo.getSize();
    }

    /**
     * 批量获取文件信息
     */
    @Transactional(readOnly = true)
    public Map<Long, PreviewInfo> getBatchPreviewInfo(List<Long> fileIds) {
        Map<Long, PreviewInfo> result = new HashMap<>();
        for (Long fileId : fileIds) {
            try {
                result.put(fileId, getPreviewInfo(fileId));
            } catch (Exception e) {
                logger.warn("获取预览信息失败: fileId={}, error={}", fileId, e.getMessage());
            }
        }
        return result;
    }
}