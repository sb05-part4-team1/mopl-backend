package com.mopl.api.application.user;

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
    public void signUp() {
        UserModel userModel = userService.signUp();
    }
}
