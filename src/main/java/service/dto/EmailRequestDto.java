package service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO для отправки email через API
 */
public class EmailRequestDto {

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotNull(message = "Тип события не может быть null")
    private UserEventDto.EventType eventType;

    public EmailRequestDto() {}

    public EmailRequestDto(String email, UserEventDto.EventType eventType) {
        this.email = email;
        this.eventType = eventType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserEventDto.EventType getEventType() {
        return eventType;
    }

    public void setEventType(UserEventDto.EventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "EmailRequestDto{" +
                "email='" + email + '\'' +
                ", eventType=" + eventType +
                '}';
    }
}