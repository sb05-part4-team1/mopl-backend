package com.mopl.jpa.repository.content;

import com.mopl.jpa.entity.content.ContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaContentRepository extends JpaRepository<ContentEntity, UUID> {
}
