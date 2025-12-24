package com.mopl.api.application.user;

import com.mopl.api.interfaces.api.user.UserCreateRequest;
import com.mopl.api.interfaces.api.user.UserDto;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;

    @Transactional
    public UserDto signUp(UserCreateRequest userCreateRequest) {
        UserModel userModel = userService.save(
            userCreateRequest.name(),
            userCreateRequest.email(),
            userCreateRequest.password()
        );

        return UserDto.from(userModel);
    }
}
