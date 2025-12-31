package com.mopl.domain.repository.user;

import com.mopl.domain.model.user.UserModel;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    UserModel save(UserModel userModel);

    Optional<UserModel> findById(UUID userId);

    Optional<UserModel> findByEmail(String email);

    boolean existsByEmail(String email);
}
