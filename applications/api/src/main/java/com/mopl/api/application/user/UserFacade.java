package com.mopl.api.application.user;

import com.mopl.api.interfaces.api.user.UserCreateRequest;
import com.mopl.api.interfaces.api.user.UserRoleUpdateRequest;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.model.user.UserModel.AuthProvider;
import com.mopl.domain.service.user.UserService;
import com.mopl.storage.provider.FileStorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;
    private final FileStorageProvider fileStorageProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserModel signUp(UserCreateRequest userCreateRequest) {
        String email = userCreateRequest.email().strip().toLowerCase(Locale.ROOT);
        String name = userCreateRequest.name().strip();
        String encodedPassword = passwordEncoder.encode(userCreateRequest.password());

        UserModel userModel = UserModel.create(
            AuthProvider.EMAIL,
            email,
            name,
            encodedPassword
        );

        return userService.create(userModel);
    }

    @Transactional(readOnly = true)
    public UserModel getUser(UUID userId) {
        return userService.getById(userId);
    }

    // @PreAuthorize("hasRole('ADMIN')")
    // @Transactional
    // public UserModel updateRole(UserRoleUpdateRequest request) {
    //     return updateRoleInternal(request, userId);
    // }

    @Transactional
    public UserModel updateRoleInternal(UserRoleUpdateRequest request, UUID userId) {
        UserModel userModel = userService.getById(userId);
        userModel.updateRole(request.role());
        return userService.update(userModel);
    }

    @Transactional
    public UserModel updateProfile(UUID userId, MultipartFile image) {
        UserModel userModel = userService.getById(userId);

        if (image != null && !image.isEmpty()) {
            try {
                String fileName = "users/"
                    + userId
                    + "/"
                    + UUID.randomUUID()
                    + "_"
                    + image.getOriginalFilename();
                String storedPath = fileStorageProvider.upload(
                    image.getInputStream(),
                    fileName
                );
                String profileImageUrl = fileStorageProvider.getUrl(storedPath);
                userModel.updateProfileImageUrl(profileImageUrl);
            } catch (IOException exception) {
                throw new UncheckedIOException("파일 스트림 읽기 실패", exception);
            }
        }

        return userService.update(userModel);
    }
}
