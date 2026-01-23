package com.mopl.api.application.user;

import com.mopl.api.application.outbox.DomainEventOutboxMapper;
import com.mopl.api.interfaces.api.user.UserCreateRequest;
import com.mopl.api.interfaces.api.user.UserLockUpdateRequest;
import com.mopl.api.interfaces.api.user.UserRoleUpdateRequest;
import com.mopl.api.interfaces.api.user.UserUpdateRequest;
import com.mopl.domain.event.user.UserRoleChangedEvent;
import com.mopl.domain.exception.user.SelfLockChangeException;
import com.mopl.domain.exception.user.SelfRoleChangeException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.TemporaryPasswordRepository;
import com.mopl.domain.repository.user.UserQueryRequest;
import com.mopl.domain.service.outbox.OutboxService;
import com.mopl.domain.service.user.UserService;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.security.jwt.registry.JwtRegistry;
import com.mopl.storage.provider.FileStorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
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
    private final TemporaryPasswordRepository temporaryPasswordRepository;
    private final JwtRegistry jwtRegistry;
    private final DomainEventOutboxMapper domainEventOutboxMapper;
    private final OutboxService outboxService;
    private final TransactionTemplate transactionTemplate;

    public UserModel signUp(UserCreateRequest userCreateRequest) {
        String email = userCreateRequest.email().strip().toLowerCase(Locale.ROOT);
        String name = userCreateRequest.name().strip();
        String encodedPassword = passwordEncoder.encode(userCreateRequest.password());

        UserModel userModel = UserModel.create(
            email,
            name,
            encodedPassword
        );

        return userService.create(userModel);
    }

    public CursorResponse<UserModel> getUsers(UserQueryRequest request) {
        return userService.getAll(request);
    }

    public UserModel getUser(UUID userId) {
        return userService.getById(userId);
    }

    public UserModel updateRole(
        UUID requesterId,
        UserRoleUpdateRequest request,
        UUID targetUserId
    ) {
        if (requesterId.equals(targetUserId)) {
            throw SelfRoleChangeException.withUserId(requesterId);
        }
        return updateRoleInternal(targetUserId, request.role());
    }

    public UserModel updateRoleInternal(UUID userId, UserModel.Role role) {
        UserModel userModel = userService.getById(userId);
        String oldRole = userModel.getRole().name();
        userModel.updateRole(role);

        UserRoleChangedEvent event = UserRoleChangedEvent.builder()
            .userId(userId)
            .oldRole(oldRole)
            .newRole(role.name())
            .build();

        UserModel updatedUser = transactionTemplate.execute(status -> {
            UserModel saved = userService.update(userModel);
            outboxService.save(domainEventOutboxMapper.toOutboxModel(event));
            return saved;
        });

        jwtRegistry.revokeAllByUserId(userId);

        return updatedUser;
    }

    public void updateLocked(
        UUID requesterId,
        UUID targetUserId,
        UserLockUpdateRequest request
    ) {
        if (requesterId.equals(targetUserId)) {
            throw SelfLockChangeException.withUserId(requesterId);
        }
        UserModel userModel = userService.getById(targetUserId);
        if (request.locked()) {
            userModel.lock();
        } else {
            userModel.unlock();
        }
        userService.update(userModel);
        jwtRegistry.revokeAllByUserId(targetUserId);
    }

    public UserModel updateProfile(UUID userId, UserUpdateRequest request, MultipartFile image) {
        UserModel userModel = userService.getById(userId);

        if (request != null && request.name() != null && !request.name().isBlank()) {
            userModel.updateName(request.name().strip());
        }

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

    public void updatePassword(UUID userId, String newPassword) {
        UserModel userModel = userService.getById(userId);
        String encodedPassword = passwordEncoder.encode(newPassword);
        userModel.updatePassword(encodedPassword);
        userService.update(userModel);

        temporaryPasswordRepository.deleteByEmail(userModel.getEmail());
    }
}
