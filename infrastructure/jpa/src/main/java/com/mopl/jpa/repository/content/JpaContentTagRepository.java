package com.mopl.jpa.repository.content;

import com.mopl.jpa.entity.content.ContentTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaContentTagRepository extends JpaRepository<ContentTagEntity, Long> {

    // @Query를 사용하여 Tag 엔티티까지 한 번에 조회
    @Query("select ct from ContentTagEntity ct join fetch ct.tag where ct.content.id = :contentId")
    List<ContentTagEntity> findAllByContentId(@Param("contentId") UUID contentId);
}
