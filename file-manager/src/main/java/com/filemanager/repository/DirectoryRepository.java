package com.filemanager.repository;

import com.filemanager.model.Directory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DirectoryRepository extends JpaRepository<Directory, Long> {

    /**
     * 查询指定父目录下的子目录列表
     */
    List<Directory> findByParentIdAndDeletedOrderByCreatedAtDesc(Long parentId, Integer deleted);

    /**
     * 根据用户ID查询根目录
     */
    List<Directory> findByUserIdAndParentIdAndDeleted(Long userId, Long parentId, Integer deleted);

    /**
     * 搜索目录（根据名称模糊查询）
     */
    @Query("SELECT d FROM Directory d WHERE d.deleted = 0 AND d.name LIKE %:keyword% AND d.userId = :userId")
    List<Directory> searchByName(String keyword, Long userId);

    /**
     * 检查同一父目录下是否存在同名目录
     */
    boolean existsByParentIdAndNameAndDeletedAndUserId(Long parentId, String name, Integer deleted, Long userId);

    /**
     * 查询所有已删除且超过30天的目录
     */
    @Query("SELECT d FROM Directory d WHERE d.deleted = 1 AND d.updatedAt < :beforeDate")
    List<Directory> findExpiredDirectories(java.time.LocalDateTime beforeDate);

    /**
     * 获取用户的根目录
     */
    @Query("SELECT d FROM Directory d WHERE d.userId = :userId AND d.parentId IS NULL AND d.deleted = 0")
    List<Directory> getRootDirectory(Long userId);
}