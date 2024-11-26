package com.eventhub.eventhub.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String resetLink) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("your-email@gmail.com");
        helper.setTo(to);
        helper.setSubject("Şifre Sıfırlama Talebi");

        String emailContent = """
            <html>
            <body>
                <h2>Şifre Sıfırlama Talebi</h2>
                <p>Şifrenizi sıfırlamak için aşağıdaki bağlantıya tıklayın:</p>
                <p><a href="%s">Şifremi Sıfırla</a></p>
                <p>Bu bağlantı 30 dakika süreyle geçerlidir.</p>
                <p>Eğer şifre sıfırlama talebinde bulunmadıysanız, bu e-postayı dikkate almayın.</p>
            </body>
            </html>
        """.formatted(resetLink);

        helper.setText(emailContent, true);
        mailSender.send(message);
    }
}