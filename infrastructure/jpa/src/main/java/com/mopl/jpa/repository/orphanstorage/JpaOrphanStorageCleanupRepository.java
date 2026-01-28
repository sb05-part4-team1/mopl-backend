package com.mopl.jpa.repository.orphanstorage;

import com.mopl.jpa.entity.content.ContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

/**
 * Storage에서 고아 파일을 정리하기 위한 Repository.
 */
public interface JpaOrphanStorageCleanupRepository extends JpaRepository<ContentEntity, UUID> {

    @Query(
        value = "select 1 from contents where thumbnail_path = :path limit 1",
        nativeQuery = true
    )
    Integer findOneByThumbnailPath(String path);

    @Query(
        value = "select 1 from users where profile_image_path = :path limit 1",
        nativeQuery = true
    )
    Integer findOneByProfileImagePath(String path);
}
