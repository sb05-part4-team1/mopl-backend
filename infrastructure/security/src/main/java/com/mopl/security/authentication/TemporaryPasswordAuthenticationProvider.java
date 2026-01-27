package com.mopl.security.authentication;

import com.mopl.domain.repository.user.TemporaryPasswordRepository;
import com.mopl.security.userdetails.MoplUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@RequiredArgsConstructor
public class TemporaryPasswordAuthenticationProvider extends DaoAuthenticationProvider {

    private final TemporaryPasswordRepository temporaryPasswordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected void additionalAuthenticationChecks(
        UserDetails userDetails,
        UsernamePasswordAuthenticationToken authentication
    ) throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            throw new BadCredentialsException("비밀번호가 입력되지 않았습니다.");
        }

        String presentedPassword = authentication.getCredentials().toString();
        String storedPassword = userDetails.getPassword();

        String email = switch (userDetails) {
            case MoplUserDetails m -> m.email();
            default -> userDetails.getUsername();
        };

        boolean mainPasswordValid = passwordEncoder.matches(presentedPassword, storedPassword);

        if (mainPasswordValid) {
            return;
        }

        Optional<String> tempPassword = temporaryPasswordRepository.findByEmail(email);

        boolean temporaryPasswordValid = tempPassword
            .map(encodedTempPassword -> passwordEncoder.matches(presentedPassword,
                encodedTempPassword))
            .orElse(false);

        if (temporaryPasswordValid) {
            return;
        }

        throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}
