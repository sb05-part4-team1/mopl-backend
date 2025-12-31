package com.mopl.jpa.repository.user;

import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.entity.user.UserEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final UserEntityMapper userEntityMapper;

    @Override
    public UserModel save(UserModel userModel) {
        UserEntity userEntity = userEntityMapper.toEntity(userModel);
        UserEntity savedUserEntity = jpaUserRepository.save(userEntity);
        return userEntityMapper.toModel(savedUserEntity);
    }

    @Override
    public Optional<UserModel> findById(UUID userId) {
        return jpaUserRepository.findById(userId)
            .map(userEntityMapper::toModel);
    }

    @Override
    public Optional<UserModel> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email)
            .map(userEntityMapper::toModel);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }
}
