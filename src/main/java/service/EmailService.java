package service;

import service.dto.UserEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Сервис для отправки email уведомлений
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String siteName;

    public EmailService(JavaMailSender mailSender,
                        @Value("${notification.site-name}") String siteName) {
        this.mailSender = mailSender;
        this.siteName = siteName;
    }


    public void sendUserEventNotification(String email, UserEventDto.EventType eventType) {
        try {
            String subject = getSubject(eventType);
            String text = getMessageText(eventType);

            sendEmail(email, subject, text);
            logger.info("Email уведомление успешно отправлено на {} для события {}",
                    email, eventType);
        } catch (Exception e) {
            logger.error("Ошибка при отправке email на {} для события {}: {}",
                    email, eventType, e.getMessage(), e);
            throw new RuntimeException("Не удалось отправить email", e);
        }
    }

    /**
     * Отправка email с указанным содержимым
     */
    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    /**
     * Получение темы письма в зависимости от типа события
     */
    private String getSubject(UserEventDto.EventType eventType) {
        return switch (eventType) {
            case USER_CREATED -> "Добро пожаловать!";
            case USER_DELETED -> "Удаление аккаунта";
        };
    }

    /**
     * Получение текста сообщения в зависимости от типа события
     */
    private String getMessageText(UserEventDto.EventType eventType) {
        return switch (eventType) {
            case USER_CREATED -> String.format(
                    "Здравствуйте! Ваш аккаунт на сайте %s был успешно создан.", siteName);
            case USER_DELETED -> "Здравствуйте! Ваш аккаунт был удалён.";
        };
    }
}
