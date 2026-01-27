package com.mopl.api.application.auth;

import com.mopl.domain.repository.user.TemporaryPasswordRepository;
import com.mopl.domain.service.user.UserService;
import com.mopl.logging.context.LogContext;
import com.mopl.mail.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AuthFacade {

    private static final int TEMP_PASSWORD_LENGTH = 12;
    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
                                                      + "0123456789!@#$%";
    private static final int TEMP_PASSWORD_EXPIRY_MINUTES = 3;

    private final UserService userService;
    private final TemporaryPasswordRepository temporaryPasswordRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public void resetPassword(String email) {
        userService.getByEmail(email);

        String temporaryPassword = generateTemporaryPassword();
        String encodedPassword = passwordEncoder.encode(temporaryPassword);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TEMP_PASSWORD_EXPIRY_MINUTES);

        temporaryPasswordRepository.save(email, encodedPassword);
        emailService.sendTemporaryPassword(email, temporaryPassword, expiresAt);

        LogContext.with("email", email).info("Password reset requested");
    }

    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            int index = random.nextInt(TEMP_PASSWORD_CHARS.length());
            password.append(TEMP_PASSWORD_CHARS.charAt(index));
        }
        return password.toString();
    }
}
