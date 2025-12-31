package com.mopl.security.userdetails;

import com.mopl.domain.model.user.UserModel;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Builder
public record MoplUserDetails(
    UUID userId,
    UserModel.Role role,
    Instant createdAt,
    String password,
    String email,
    String name,
    String profileImageUrl,
    Boolean locked
) implements UserDetails {

    public static MoplUserDetails from(UserModel user) {
        return MoplUserDetails.builder()
            .userId(user.getId())
            .role(user.getRole())
            .createdAt(user.getCreatedAt())
            .password(user.getPassword())
            .email(user.getEmail())
            .name(user.getName())
            .profileImageUrl(user.getProfileImageUrl())
            .locked(user.isLocked())
            .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userId.toString();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !Boolean.TRUE.equals(locked);
    }
}
