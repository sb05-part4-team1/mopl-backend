package com.mopl.domain.repository.content;

import java.util.UUID;

public interface ContentRepository {

    boolean existsById(UUID contentId);
}
