package com.mopl.domain.service.user;

import com.mopl.domain.exception.user.DuplicateEmailException;
import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.UserQueryRepository;
import com.mopl.domain.repository.user.UserQueryRequest;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserQueryRepository userQueryRepository;

    public UserModel create(UserModel userModel) {
        validateDuplicateEmail(userModel.getEmail());
        return userRepository.save(userModel);
    }

    public CursorResponse<UserModel> getAll(UserQueryRequest request) {
        return userQueryRepository.findAll(request);
    }

    public UserModel getById(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.withId(userId));
    }

    public UserModel getByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> UserNotFoundException.withEmail(email));
    }

    public UserModel update(UserModel userModel) {
        return userRepository.save(userModel);
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw DuplicateEmailException.withEmail(email);
        }
    }
}
