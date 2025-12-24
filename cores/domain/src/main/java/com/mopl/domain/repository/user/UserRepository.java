package com.mopl.domain.repository.user;

import com.mopl.domain.model.user.UserModel;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository {

    UserModel save(UserModel userModel);

    Optional<UserModel> findByEmail(String email);

    boolean existsByEmail(String email);
}
