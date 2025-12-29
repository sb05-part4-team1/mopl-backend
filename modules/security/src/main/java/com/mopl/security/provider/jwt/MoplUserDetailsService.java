package com.mopl.security.provider.jwt;

import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RequiredArgsConstructor
public class MoplUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            UserModel user = userService.getByEmail(email);

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
        } catch (UserNotFoundException e) {
            throw new UsernameNotFoundException("존재하지 않는 사용자 이메일입니다: " + email);
        }
    }
}
