package studi.doryanbessiere.jo2024.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import studi.doryanbessiere.jo2024.notifications.dto.EmailRequest;
import studi.doryanbessiere.jo2024.rendering.TemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailNotificationService implements NotificationService<EmailRequest> {

    private final JavaMailSenderImpl mailSender;
    private final TemplateEngine templateEngine;

    @Override
    public void sendNotification(EmailRequest request) {
        String body = request.getMessage();

        if (request.getTemplateName() != null && request.getVariables() != null) {
            body = templateEngine.render(request.getTemplateName(), request.getVariables());
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailSender.getUsername());
        message.setTo(request.getTo());
        message.setSubject(request.getSubject());
        message.setText(body);

        mailSender.send(message);
    }
}