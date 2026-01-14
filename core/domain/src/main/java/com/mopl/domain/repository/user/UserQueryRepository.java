package com.mopl.domain.repository.user;

import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.support.cursor.CursorResponse;
import java.util.UUID;

public interface UserQueryRepository {

    CursorResponse<UserModel> findAll(UserQueryRequest request);
}
