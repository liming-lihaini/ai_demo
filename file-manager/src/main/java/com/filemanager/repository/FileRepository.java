package com.filemanager.repository;

import com.filemanager.model.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文件数据访问层
 */
@Repository
public interface FileRepository extends JpaRepository<FileInfo, Long> {

    /**
     * 查询指定目录下的文件列表
     */
    List<FileInfo> findByParentIdAndDeletedOrderByCreatedAtDesc(Long parentId, Integer deleted);

    /**
     * 根据用户ID和目录查询文件
     */
    List<FileInfo> findByUserIdAndParentIdAndDeleted(Long userId, Long parentId, Integer deleted);

    /**
     * 检查同一目录下是否存在同名文件
     */
    boolean existsByParentIdAndNameAndDeletedAndUserId(Long parentId, String name, Integer deleted, Long userId);

    /**
     * 根据 MD5 和 parentId 检查是否已存在（用于去重）
     */
    boolean existsByMd5AndParentIdAndDeleted(String md5, Long parentId, Integer deleted);

    /**
     * 搜索文件（根据文件名模糊查询）
     */
    @Query("SELECT f FROM FileInfo f WHERE f.deleted = 0 AND f.name LIKE %:keyword% AND f.userId = :userId")
    List<FileInfo> searchByName(String keyword, Long userId);

    /**
     * 查询所有已删除且超过30天的文件
     */
    @Query("SELECT f FROM FileInfo f WHERE f.deleted = 1 AND f.updatedAt < :beforeDate")
    List<FileInfo> findExpiredFiles(java.time.LocalDateTime beforeDate);

    /**
     * 批量查询文件
     */
    List<FileInfo> findByIdIn(List<Long> ids);
}