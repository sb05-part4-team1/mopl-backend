package com.mopl.api.interfaces.api.user;

import com.mopl.api.application.user.UserFacade;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.UserQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.security.userdetails.MoplUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
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
    @PreAuthorize("hasRole('ADMIN')")
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
        @RequestPart(value = "request", required = false) @Valid UserUpdateRequest request,
        @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        UserModel userModel = userFacade.updateProfile(userId, request, image);
        return userResponseMapper.toResponse(userModel);
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRole(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID userId,
        @RequestBody @Valid UserRoleUpdateRequest request
    ) {
        userFacade.updateRole(userDetails.userId(), request, userId);
    }

    @PatchMapping("/{userId}/locked")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateLocked(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID userId,
        @RequestBody @Valid UserLockUpdateRequest request
    ) {
        userFacade.updateLocked(userDetails.userId(), userId, request);
    }
}
