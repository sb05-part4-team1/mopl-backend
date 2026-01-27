package com.mopl.security.authentication;

import com.mopl.domain.repository.user.TemporaryPasswordRepository;
import com.mopl.security.userdetails.MoplUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Slf4j
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

        log.debug("인증 시도: email={}", email);

        boolean mainPasswordValid = passwordEncoder.matches(presentedPassword, storedPassword);
        log.debug("기존 비밀번호 검증: email={}, valid={}", email, mainPasswordValid);

        if (mainPasswordValid) {
            return;
        }

        Optional<String> tempPassword = temporaryPasswordRepository.findByEmail(email);
        log.debug("임시 비밀번호 존재 여부: email={}, exists={}", email, tempPassword.isPresent());

        boolean temporaryPasswordValid = tempPassword
            .map(encodedTempPassword -> passwordEncoder.matches(presentedPassword,
                encodedTempPassword))
            .orElse(false);
        log.debug("임시 비밀번호 검증: email={}, valid={}", email, temporaryPasswordValid);

        if (temporaryPasswordValid) {
            return;
        }

        throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}
