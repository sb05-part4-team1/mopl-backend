package com.mopl.security.provider.jwt;

import com.mopl.domain.model.user.UserModel;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record MoplUserDetails(
    UUID userId,
    Instant createdAt,
    String password,
    String email,
    String name,
    String profileImageUrl,
    UserModel.Role role,
    boolean locked
) implements UserDetails {

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
}
