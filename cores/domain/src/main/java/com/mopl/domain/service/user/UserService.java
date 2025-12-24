package com.mopl.domain.service.user;

import com.mopl.domain.exception.user.DuplicateEmailException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserModel create(UserModel userModel) {
        validateDuplicateEmail(userModel.getEmail());
        return userRepository.save(userModel);
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }
}
