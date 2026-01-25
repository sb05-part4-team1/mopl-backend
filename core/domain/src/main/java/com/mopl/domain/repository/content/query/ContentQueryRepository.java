package com.mopl.domain.repository.content.query;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.support.cursor.CursorResponse;

public interface ContentQueryRepository {

    CursorResponse<ContentModel> findAll(ContentQueryRequest request);
}
