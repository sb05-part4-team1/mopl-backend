package com.mopl.mail.service;

import com.mopl.logging.context.LogContext;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm:ss");

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendTemporaryPassword(String to, String temporaryPassword,
                                      LocalDateTime expiresAt) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("임시 비밀번호 발급 - MOPL");
            helper.setText(buildTemporaryPasswordEmailHtml(temporaryPassword, expiresAt), true);

            mailSender.send(message);
            LogContext.with("to", to).and("type", "temporaryPassword").info("Email sent");
        } catch (MessagingException e) {
            LogContext.with("to", to).and("type", "temporaryPassword").error("Email send failed", e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }

    private String buildTemporaryPasswordEmailHtml(String temporaryPassword,
                                                   LocalDateTime expiresAt) {
        String formattedExpiresAt = expiresAt.format(DATE_FORMATTER);

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 0;
                        padding: 20px;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: white;
                        padding: 30px;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .header { text-align: center; margin-bottom: 30px; }
                    .logo { font-size: 24px; font-weight: bold; color: #333; }
                    .password-box {
                        background-color: #f8f9fa;
                        padding: 20px;
                        border-radius: 5px;
                        text-align: center;
                        margin: 20px 0;
                        border-left: 4px solid #007bff;
                    }
                    .password {
                        font-size: 24px;
                        font-weight: bold;
                        color: #007bff;
                        letter-spacing: 2px;
                    }
                    .warning {
                        background-color: #fff3cd;
                        padding: 15px;
                        border-radius: 5px;
                        border-left: 4px solid #ffc107;
                        margin: 20px 0;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 30px;
                        color: #666;
                        font-size: 12px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">MOPL</div>
                        <h2>임시 비밀번호가 발급되었습니다</h2>
                    </div>
            
                    <p>안녕하세요!</p>
                    <p>요청하신 임시 비밀번호가 발급되었습니다. 아래 임시 비밀번호를 사용하여 로그인 후 새로운 비밀번호로 변경해주세요.</p>
            
                    <div class="password-box">
                        <div>임시 비밀번호</div>
                        <div class="password">%s</div>
                    </div>
            
                    <div class="warning">
                        <strong>⚠️ 중요 안내사항</strong><br>
                        • 이 임시 비밀번호는 <strong>%s</strong>까지만 유효합니다<br>
                        • 보안을 위해 로그인 후 즉시 새로운 비밀번호로 변경해주세요<br>
                        • 임시 비밀번호는 다른 사람과 공유하지 마세요
                    </div>
            
                    <div class="footer">
                        본 메일은 발신전용이므로 회신되지 않습니다.<br>
                        문의사항이 있으시면 고객센터로 연락해주세요.
                    </div>
                </div>
            </body>
            </html>
            """
            .formatted(temporaryPassword, formattedExpiresAt);
    }
}
