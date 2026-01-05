package com.mopl.api.interfaces.api.user;

import com.mopl.api.application.user.UserFacade;
import com.mopl.domain.model.user.UserModel;
import com.mopl.jpa.repository.user.query.UserQueryRequest;
import com.mopl.jpa.support.cursor.CursorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

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

    @GetMapping
    public CursorResponse<UserResponse> getUsers(@ModelAttribute UserQueryRequest request) {
        return userFacade.getUsers(request);
    }

    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable UUID userId) {
        UserModel userModel = userFacade.getUser(userId);
        return userResponseMapper.toResponse(userModel);
    }

    @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse updateProfile(
        @PathVariable UUID userId,
        @RequestParam("image") MultipartFile image
    ) {
        UserModel userModel = userFacade.updateProfile(userId, image);
        return userResponseMapper.toResponse(userModel);
    }
}
