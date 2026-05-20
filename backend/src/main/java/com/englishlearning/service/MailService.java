package com.englishlearning.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.enabled}")
    private boolean enabled;

    public void sendPasswordResetEmail(String to, String token) {
        String link = baseUrl + "/restablecer?token=" + token;
        String subject = "Recupera tu contraseña — enclave";
        String html = """
                <div style="font-family:sans-serif;padding:2rem;max-width:480px;margin:0 auto">
                    <h2 style="color:#e87b6d">enclave · inglés CEFR</h2>
                    <p>Hemos recibido una solicitud para restablecer tu contraseña.</p>
                    <p>Haz clic en el botón de abajo para crear una nueva contraseña:</p>
                    <a href="%s"
                       style="display:inline-block;padding:0.75rem 1.5rem;background:#e87b6d;color:#fff;
                              text-decoration:none;border-radius:10px;margin:1rem 0">
                        Restablecer contraseña
                    </a>
                    <p style="color:#9b8aa1;font-size:0.85rem">
                        Si no solicitaste este cambio, ignora este correo.
                        <br>Este enlace expira en 1 hora.
                    </p>
                </div>
                """.formatted(link);

        if (!enabled) {
            log.warn("[MAIL DISABLED] Password reset token for {}: {}", to, token);
            return;
        }

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("Password reset email sent to {}", to);
        } catch (MessagingException e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
            log.warn("[FALLBACK] Password reset token for {}: {}", to, token);
        }
    }
}
