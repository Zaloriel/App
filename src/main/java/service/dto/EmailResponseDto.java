package service.dto;

import java.time.LocalDateTime;

/**
 * DTO для ответа на отправку email
 */
public class EmailResponseDto {

    private boolean success;
    private String message;
    private String email;
    private LocalDateTime timestamp;

    public EmailResponseDto() {
        this.timestamp = LocalDateTime.now();
    }

    public EmailResponseDto(boolean success, String message, String email) {
        this.success = success;
        this.message = message;
        this.email = email;
        this.timestamp = LocalDateTime.now();
    }

    public static EmailResponseDto success(String email, String message) {
        return new EmailResponseDto(true, message, email);
    }

    public static EmailResponseDto error(String email, String message) {
        return new EmailResponseDto(false, message, email);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "EmailResponseDto{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", email='" + email + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}