package service.kafka;

import service.dto.UserEventDto;
import service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer для обработки событий пользователя
 */
@Component
public class UserEventKafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UserEventKafkaConsumer.class);

    private final EmailService emailService;

    public UserEventKafkaConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Обработка событий пользователя из Kafka
     */
    @KafkaListener(
            topics = "${notification.topics.user-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserEvent(
            @Payload UserEventDto userEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        logger.info("Получено сообщение из Kafka - Topic: {}, Partition: {}, Offset: {}, Event: {}",
                topic, partition, offset, userEvent);

        try {
            // Валидация входящего события
            validateUserEvent(userEvent);

            // Отправка email уведомления
            emailService.sendUserEventNotification(userEvent.getEmail(), userEvent.getEventType());

            // Подтверждение обработки сообщения
            acknowledgment.acknowledge();

            logger.info("Событие пользователя успешно обработано: {}", userEvent);

        } catch (Exception e) {
            logger.error("Ошибка при обработке события пользователя {}: {}",
                    userEvent, e.getMessage(), e);

            // В зависимости от стратегии обработки ошибок, можно:
            // 1. Не подтверждать сообщение (оно будет повторно обработано)
            // 2. Отправить в DLQ (Dead Letter Queue)
            // 3. Записать в лог и продолжить

            // Для примера подтверждаем даже с ошибкой, чтобы не блокировать очередь
            acknowledgment.acknowledge();
        }
    }

    /**
     * Валидация события пользователя
     */
    private void validateUserEvent(UserEventDto userEvent) {
        if (userEvent == null) {
            throw new IllegalArgumentException("UserEvent не может быть null");
        }

        if (userEvent.getEmail() == null || userEvent.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email пользователя не может быть пустым");
        }

        if (userEvent.getEventType() == null) {
            throw new IllegalArgumentException("Тип события не может быть null");
        }

        // Простая валидация email
        if (!userEvent.getEmail().contains("@")) {
            throw new IllegalArgumentException("Некорректный формат email: " + userEvent.getEmail());
        }
    }
}
