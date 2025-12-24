package com.mopl.domain.repository.user;

import com.mopl.domain.model.user.UserModel;

public interface UserRepository {

    UserModel save(UserModel userModel);

    boolean existsByEmail(String email);
}
