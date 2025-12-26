package com.mopl.api.interfaces.api.user;

import com.mopl.api.application.user.UserFacade;
import com.mopl.domain.model.user.UserModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApiSpec {

    private final UserFacade userFacade;
    private final UserResponseMapper userResponseMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signUp(@RequestBody @Valid UserCreateRequest request) {
        UserModel userModel = userFacade.signUp(request);
        return userResponseMapper.toResponse(userModel);
    }
}
