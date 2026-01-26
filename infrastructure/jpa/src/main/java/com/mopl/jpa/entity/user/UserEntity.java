package com.mopl.jpa.entity.user;

import com.mopl.domain.model.user.UserModel;
import com.mopl.jpa.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import static com.mopl.domain.model.user.UserModel.AUTH_PROVIDER_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.NAME_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.PROFILE_IMAGE_PATH_MAX_LENGTH;
import static com.mopl.domain.model.user.UserModel.ROLE_MAX_LENGTH;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@SQLRestriction("deleted_at IS NULL")
public class UserEntity extends BaseUpdatableEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = AUTH_PROVIDER_MAX_LENGTH)
    private UserModel.AuthProvider authProvider;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Column
    private String password;

    @Column(length = PROFILE_IMAGE_PATH_MAX_LENGTH)
    private String profileImagePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = ROLE_MAX_LENGTH)
    private UserModel.Role role;

    @Column(nullable = false)
    private boolean locked;
}
