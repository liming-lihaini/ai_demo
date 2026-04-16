package com.filemanager.dto;

import lombok.Data;

/**
 * 目录创建/更新请求DTO
 */
@Data
public class DirectoryDTO {

    private Long id;

    private String name;

    private Long parentId;

    private Long userId;
}