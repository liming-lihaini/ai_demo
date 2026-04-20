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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PreviewService {

    private static final Logger logger = LoggerFactory.getLogger(PreviewService.class);

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private StorageService storageService;

    @Value("${storage.path:./storage/files}")
    private String storagePath;

    private static final List<String> DIRECT_PREVIEW_FORMATS = Arrays.asList(
            "pdf", "txt", "md",
            "jpg", "jpeg", "png", "gif", "bmp",
            "mp3", "wav", "mp4", "avi"
    );

    private static final List<String> IMAGE_FORMATS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp"
    );

    private static final List<String> AUDIO_FORMATS = Arrays.asList(
            "mp3", "wav"
    );

    private static final List<String> VIDEO_FORMATS = Arrays.asList(
            "mp4", "avi"
    );

    private static final List<String> TEXT_FORMATS = Arrays.asList(
            "txt", "md"
    );

    private static final List<String> OFFICE_FORMATS = Arrays.asList(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx"
    );

    private static final long LARGE_FILE_THRESHOLD = 20 * 1024 * 1024;

    @Transactional(readOnly = true)
    public PreviewInfo getPreviewInfo(Long fileId) {
        FileInfo fileInfo = fileRepository.selectById(fileId);
        if (fileInfo == null) {
            throw new RuntimeException("文件不存在");
        }

        if (fileInfo.getDeleted() == 1) {
            throw new RuntimeException("文件已删除");
        }

        PreviewInfo info = new PreviewInfo();
        info.setFileId(fileInfo.getId());
        info.setFileName(fileInfo.getName());
        info.setFormat(fileInfo.getFormat());
        info.setSize(fileInfo.getSize());

        String format = fileInfo.getFormat().toLowerCase();
        String previewType = getPreviewType(format);
        info.setPreviewType(previewType);

        boolean needConvert = OFFICE_FORMATS.contains(format) && previewType.equals("text");
        info.setNeedConvert(needConvert);

        return info;
    }

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

    @Transactional(readOnly = true)
    public Resource getPreviewContent(Long fileId) throws IOException {
        FileInfo fileInfo = fileRepository.selectById(fileId);
        if (fileInfo == null) {
            throw new RuntimeException("文件不存在");
        }

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

    @Transactional(readOnly = true)
    public byte[] getPdfPreview(Long fileId) throws IOException {
        FileInfo fileInfo = fileRepository.selectById(fileId);
        if (fileInfo == null) {
            throw new RuntimeException("文件不存在");
        }

        if (fileInfo.getDeleted() == 1) {
            throw new RuntimeException("文件已删除");
        }

        String format = fileInfo.getFormat().toLowerCase();

        if (format.equals("pdf")) {
            Path fullPath = Paths.get(storagePath, fileInfo.getPath());
            return Files.readAllBytes(fullPath);
        }

        if (OFFICE_FORMATS.contains(format)) {
            Path fullPath = Paths.get(storagePath, fileInfo.getPath());
            try (InputStream inputStream = Files.newInputStream(fullPath)) {
                return OfficeConverter.convertToText(inputStream, format).getBytes(StandardCharsets.UTF_8);
            }
        }

        throw new IOException("不支持的格式: " + format);
    }

    @Transactional(readOnly = true)
    public String getTextPreview(Long fileId) throws IOException {
        FileInfo fileInfo = fileRepository.selectById(fileId);
        if (fileInfo == null) {
            throw new RuntimeException("文件不存在");
        }

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

    @Transactional(readOnly = true)
    public String getHtmlPreview(Long fileId) throws IOException {
        FileInfo fileInfo = fileRepository.selectById(fileId);
        if (fileInfo == null) {
            throw new RuntimeException("文件不存在");
        }

        if (fileInfo.getDeleted() == 1) {
            throw new RuntimeException("文件已删除");
        }

        String format = fileInfo.getFormat().toLowerCase();

        if (OFFICE_FORMATS.contains(format)) {
            Path fullPath = Paths.get(storagePath, fileInfo.getPath());
            try (InputStream inputStream = Files.newInputStream(fullPath)) {
                return OfficeConverter.convertToHtml(inputStream, format);
            }
        }

        if (format.equals("pdf")) {
            Path fullPath = Paths.get(storagePath, fileInfo.getPath());
            try (PDDocument document = PDDocument.load(fullPath.toFile())) {
                PDFTextStripper stripper = new PDFTextStripper();
                return textToHtml(stripper.getText(document));
            }
        }

        throw new IOException("不支持的格式: " + format);
    }

    private String textToHtml(String text) {
        String escaped = text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");

        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">" +
                "<style>body{font-family:Arial,sans-serif;padding:20px;line-height:1.6;}</style>" +
                "</head><body>" + escaped + "</body></html>";
    }

    @Transactional(readOnly = true)
    public Resource getThumbnail(Long fileId) throws IOException {
        FileInfo fileInfo = fileRepository.selectById(fileId);
        if (fileInfo == null) {
            throw new RuntimeException("文件不存在");
        }

        if (fileInfo.getDeleted() == 1) {
            throw new RuntimeException("文件已删除");
        }

        String format = fileInfo.getFormat().toLowerCase();

        if (IMAGE_FORMATS.contains(format)) {
            Path fullPath = Paths.get(storagePath, fileInfo.getPath());
            return new UrlResource(fullPath.toUri());
        }

        throw new IOException("不支持生成缩略图: " + format);
    }

    public boolean isLargeFile(Long fileId) {
        FileInfo fileInfo = fileRepository.selectById(fileId);
        return fileInfo != null && fileInfo.getSize() > LARGE_FILE_THRESHOLD;
    }

    public long getFileSize(Long fileId) {
        FileInfo fileInfo = fileRepository.selectById(fileId);
        if (fileInfo == null) {
            throw new RuntimeException("文件不存在");
        }
        return fileInfo.getSize();
    }

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