package com.mopl.jpa.repository.user;

import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.jpa.entity.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public UserModel save(UserModel userModel) {
        UserEntity userEntity = UserEntity.from(userModel);
        UserEntity savedUserEntity = jpaUserRepository.save(userEntity);
        return savedUserEntity.toModel();
    }

    @Override
    public Optional<UserModel> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email)
            .map(UserEntity::toModel);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }
}
