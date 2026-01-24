package com.mopl.jpa.repository.tag;

import com.mopl.jpa.entity.tag.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaTagRepository extends JpaRepository<TagEntity, UUID> {

    Optional<TagEntity> findByName(String tagName);

    List<TagEntity> findByNameIn(List<String> tagNames);
}
