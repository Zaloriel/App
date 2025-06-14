package controller;

import service.dto.EmailRequestDto;
import service.dto.EmailResponseDto;
import service.EmailService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST контроллер для отправки email уведомлений
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Отправка email уведомления через API
     */
    @PostMapping("/email")
    public ResponseEntity<EmailResponseDto> sendEmail(@Valid @RequestBody EmailRequestDto request) {
        logger.info("Получен запрос на отправку email: {}", request);

        try {
            emailService.sendUserEventNotification(request.getEmail(), request.getEventType());

            EmailResponseDto response = EmailResponseDto.success(
                    request.getEmail(),
                    "Email уведомление успешно отправлено"
            );

            logger.info("Email успешно отправлен на {}", request.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Ошибка при отправке email на {}: {}",
                    request.getEmail(), e.getMessage(), e);

            EmailResponseDto response = EmailResponseDto.error(
                    request.getEmail(),
                    "Не удалось отправить email: " + e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Проверка состояния сервиса
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running");
    }
}