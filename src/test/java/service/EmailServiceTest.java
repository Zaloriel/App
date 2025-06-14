package service;

import service.dto.UserEventDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    private static final String SITE_NAME = "Тестовый Сайт";
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, SITE_NAME);
    }

    @Test
    void shouldSendUserCreatedEmail() {
        // Given
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendUserEventNotification(TEST_EMAIL, UserEventDto.EventType.USER_CREATED);

        // Then
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTo()).containsExactly(TEST_EMAIL);
        assertThat(sentMessage.getSubject()).isEqualTo("Добро пожаловать!");
        assertThat(sentMessage.getText()).isEqualTo(
                "Здравствуйте! Ваш аккаунт на сайте " + SITE_NAME + " был успешно создан."
        );
    }

    @Test
    void shouldSendUserDeletedEmail() {
        // Given
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendUserEventNotification(TEST_EMAIL, UserEventDto.EventType.USER_DELETED);

        // Then
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTo()).containsExactly(TEST_EMAIL);
        assertThat(sentMessage.getSubject()).isEqualTo("Удаление аккаунта");
        assertThat(sentMessage.getText()).isEqualTo("Здравствуйте! Ваш аккаунт был удалён.");
    }

    @Test
    void shouldThrowExceptionWhenMailSenderFails() {
        // Given
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        assertThatThrownBy(() ->
                emailService.sendUserEventNotification(TEST_EMAIL, UserEventDto.EventType.USER_CREATED)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Не удалось отправить email");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
