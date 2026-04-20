package com.filemanager.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件
 */
@Data
@TableName("files")
public class FileInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String format;

    private Long size;

    private String md5;

    private String path;

    private Long parentId;

    private Long userId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    private LocalDateTime deleteAt;
}