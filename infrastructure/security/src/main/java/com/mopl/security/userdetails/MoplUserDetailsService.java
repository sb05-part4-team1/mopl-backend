package com.mopl.security.userdetails;

import com.mopl.domain.exception.user.UserNotFoundException;
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
            return MoplUserDetails.from(userService.getByEmail(email));
        } catch (UserNotFoundException e) {
            throw new UsernameNotFoundException("존재하지 않는 사용자 이메일입니다: " + email);
        }
    }
}
