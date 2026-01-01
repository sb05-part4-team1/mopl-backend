package com.mopl.jpa.repository.content;

import com.mopl.jpa.entity.content.ContentTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaContentTagRepository extends JpaRepository<ContentTagEntity, UUID> {
}
