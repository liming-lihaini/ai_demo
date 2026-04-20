package com.filemanager.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.filemanager.model.FileInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileRepository extends BaseMapper<FileInfo> {
}