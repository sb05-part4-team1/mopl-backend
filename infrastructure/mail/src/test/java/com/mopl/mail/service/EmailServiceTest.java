package com.mopl.mail.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService 단위 테스트")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@mopl.com");
    }

    @Nested
    @DisplayName("sendTemporaryPassword()")
    class SendTemporaryPasswordTest {

        @Test
        @DisplayName("유효한 파라미터로 호출 시 이메일 발송 성공")
        void withValidParams_sendsEmail() {
            // given
            String to = "test@example.com";
            String temporaryPassword = "tempPass123!";
            LocalDateTime expiresAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

            MimeMessage mimeMessage = mock(MimeMessage.class);
            given(mailSender.createMimeMessage()).willReturn(mimeMessage);

            // when
            emailService.sendTemporaryPassword(to, temporaryPassword, expiresAt);

            // then
            then(mailSender).should().createMimeMessage();
            then(mailSender).should().send(mimeMessage);
        }

        @Test
        @DisplayName("MimeMessage 생성 후 send가 호출된다")
        void afterCreatingMimeMessage_sendIsCalled() {
            // given
            String to = "user@example.com";
            String temporaryPassword = "pass123!";
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(3);

            MimeMessage mimeMessage = mock(MimeMessage.class);
            given(mailSender.createMimeMessage()).willReturn(mimeMessage);

            // when
            emailService.sendTemporaryPassword(to, temporaryPassword, expiresAt);

            // then
            ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
            then(mailSender).should().send(messageCaptor.capture());
            assertThat(messageCaptor.getValue()).isEqualTo(mimeMessage);
        }
    }
}
