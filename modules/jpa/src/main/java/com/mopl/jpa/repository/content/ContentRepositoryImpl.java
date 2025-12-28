package com.mopl.jpa.repository.content;

import com.mopl.domain.repository.content.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepository {

    private final JpaContentRepository jpaContentRepository;

    @Override
    public boolean existsById(UUID contentId) {
        return jpaContentRepository.existsById(contentId);
    }
}
