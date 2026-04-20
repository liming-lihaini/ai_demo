package com.filemanager.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.filemanager.model.Directory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DirectoryRepository extends BaseMapper<Directory> {
}