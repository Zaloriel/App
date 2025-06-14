package service.kafka;

import service.dto.UserEventDto;
import service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

/**
 * Unit тесты для UserEventKafkaConsumer
 */
@ExtendWith(MockitoExtension.class)
public class UserEventKafkaConsumerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private Acknowledgment acknowledgment;

    private UserEventKafkaConsumer kafkaConsumer;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_TOPIC = "user-events";
    private static final int TEST_PARTITION = 0;
    private static final long TEST_OFFSET = 123L;

    @BeforeEach
    void setUp() {
        kafkaConsumer = new UserEventKafkaConsumer(emailService);
    }

    @Test
    void shouldProcessUserCreatedEvent() {
        // Given
        UserEventDto userEvent = new UserEventDto(
                TEST_EMAIL,
                UserEventDto.EventType.USER_CREATED,
                LocalDateTime.now()
        );

        // When
        kafkaConsumer.handleUserEvent(userEvent, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, acknowledgment);

        // Then
        verify(emailService).sendUserEventNotification(TEST_EMAIL, UserEventDto.EventType.USER_CREATED);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldProcessUserDeletedEvent() {
        // Given
        UserEventDto userEvent = new UserEventDto(
                TEST_EMAIL,
                UserEventDto.EventType.USER_DELETED,
                LocalDateTime.now()
        );

        // When
        kafkaConsumer.handleUserEvent(userEvent, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, acknowledgment);

        // Then
        verify(emailService).sendUserEventNotification(TEST_EMAIL, UserEventDto.EventType.USER_DELETED);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldAcknowledgeMessageEvenWhenEmailServiceFails() {
        // Given
        UserEventDto userEvent = new UserEventDto(
                TEST_EMAIL,
                UserEventDto.EventType.USER_CREATED,
                LocalDateTime.now()
        );

        doThrow(new RuntimeException("Email service error"))
                .when(emailService).sendUserEventNotification(any(), any());

        // When
        kafkaConsumer.handleUserEvent(userEvent, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, acknowledgment);

        // Then
        verify(emailService).sendUserEventNotification(TEST_EMAIL, UserEventDto.EventType.USER_CREATED);
        verify(acknowledgment).acknowledge(); // Сообщение должно быть подтверждено даже при ошибке
    }

    @Test
    void shouldAcknowledgeMessageWhenUserEventIsNull() {
        // When
        kafkaConsumer.handleUserEvent(null, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, acknowledgment);

        // Then
        verify(emailService, never()).sendUserEventNotification(any(), any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldAcknowledgeMessageWhenEmailIsEmpty() {
        // Given
        UserEventDto userEvent = new UserEventDto(
                "",
                UserEventDto.EventType.USER_CREATED,
                LocalDateTime.now()
        );

        // When
        kafkaConsumer.handleUserEvent(userEvent, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, acknowledgment);

        // Then
        verify(emailService, never()).sendUserEventNotification(any(), any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldAcknowledgeMessageWhenEventTypeIsNull() {
        // Given
        UserEventDto userEvent = new UserEventDto(
                TEST_EMAIL,
                null,
                LocalDateTime.now()
        );

        // When
        kafkaConsumer.handleUserEvent(userEvent, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, acknowledgment);

        // Then
        verify(emailService, never()).sendUserEventNotification(any(), any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldAcknowledgeMessageWhenEmailFormatIsInvalid() {
        // Given
        UserEventDto userEvent = new UserEventDto(
                "invalid-email",
                UserEventDto.EventType.USER_CREATED,
                LocalDateTime.now()
        );

        // When
        kafkaConsumer.handleUserEvent(userEvent, TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, acknowledgment);

        // Then
        verify(emailService, never()).sendUserEventNotification(any(), any());
        verify(acknowledgment).acknowledge();
    }
}
