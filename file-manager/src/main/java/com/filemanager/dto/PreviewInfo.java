package com.filemanager.dto;

import lombok.Data;

/**
 * 预览信息 DTO
 */
@Data
public class PreviewInfo {

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件格式
     */
    private String format;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 预览类型: pdf, image, text, audio, video, office
     */
    private String previewType;

    /**
     * 是否需要转换
     */
    private Boolean needConvert;

    /**
     * 预览URL
     */
    private String url;

    /**
     * 错误信息（如有）
     */
    private String error;
}