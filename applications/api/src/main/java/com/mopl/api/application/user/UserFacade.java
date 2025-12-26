package com.mopl.api.application.user;

import com.mopl.api.interfaces.api.user.UserCreateRequest;
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
    public UserModel signUp(UserCreateRequest userCreateRequest) {
        String email = userCreateRequest.email().strip().toLowerCase(Locale.ROOT);
        String name = userCreateRequest.name().strip();
        // password 암호화 로직 추가 필요
        String password = userCreateRequest.password();

        UserModel userModel = UserModel.create(
            AuthProvider.EMAIL,
            email,
            name,
            password
        );

        return userService.create(userModel);
    }
}
