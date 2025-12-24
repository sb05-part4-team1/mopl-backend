package com.mopl.domain.repository.user;

import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.base.CrudRepository;

import java.util.UUID;

public interface UserRepository extends CrudRepository<UserModel, UUID> {

    boolean existsByEmail(String email);
}
