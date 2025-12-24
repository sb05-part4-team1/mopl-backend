package com.mopl.api.application.user;

import com.mopl.api.interfaces.api.user.UserCreateRequest;
import com.mopl.api.interfaces.api.user.UserDto;
import com.mopl.domain.model.user.AuthProvider;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;

    @Transactional
    public UserDto signUp(UserCreateRequest userCreateRequest) {
        String name = userCreateRequest.name().strip();
        String email = userCreateRequest.email().strip().toLowerCase(Locale.ROOT);
        String password = userCreateRequest.password();
        UserModel userModel = UserModel.create(
            AuthProvider.EMAIL,
            name,
            email,
            password
        );

        UserModel savedUserModel = userService.create(userModel);

        return UserDto.from(savedUserModel);
    }
}
