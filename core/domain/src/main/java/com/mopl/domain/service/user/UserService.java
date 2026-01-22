package com.mopl.domain.service.user;

import com.mopl.domain.exception.user.DuplicateEmailException;
import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.UserQueryRepository;
import com.mopl.domain.repository.user.UserQueryRequest;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.domain.support.cache.CacheName;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.util.UUID;

@RequiredArgsConstructor
public class UserService {

    private final UserQueryRepository userQueryRepository;
    private final UserRepository userRepository;

    public CursorResponse<UserModel> getAll(UserQueryRequest request) {
        return userQueryRepository.findAll(request);
    }

    @Cacheable(cacheNames = CacheName.USERS, key = "#userId")
    public UserModel getById(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.withId(userId));
    }

    @Cacheable(cacheNames = CacheName.USERS_BY_EMAIL, key = "#email")
    public UserModel getByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> UserNotFoundException.withEmail(email));
    }

    @Caching(put = {
        @CachePut(cacheNames = CacheName.USERS, key = "#result.id"),
        @CachePut(cacheNames = CacheName.USERS_BY_EMAIL, key = "#result.email")
    })
    public UserModel create(UserModel userModel) {
        validateDuplicateEmail(userModel.getEmail());
        return userRepository.save(userModel);
    }

    @Caching(put = {
        @CachePut(cacheNames = CacheName.USERS, key = "#result.id"),
        @CachePut(cacheNames = CacheName.USERS_BY_EMAIL, key = "#result.email")
    })
    public UserModel update(UserModel userModel) {
        return userRepository.save(userModel);
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw DuplicateEmailException.withEmail(email);
        }
    }
}
