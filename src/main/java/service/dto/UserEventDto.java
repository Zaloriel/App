package service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO для событий пользователя из Kafka
 */
public class UserEventDto {

    private final String email;
    private final EventType eventType;
    private final LocalDateTime timestamp;

    @JsonCreator
    public UserEventDto(
            @JsonProperty("email") String email,
            @JsonProperty("eventType") EventType eventType,
            @JsonProperty("timestamp") LocalDateTime timestamp) {
        this.email = email;
        this.eventType = eventType;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public EventType getEventType() {
        return eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEventDto that = (UserEventDto) o;
        return Objects.equals(email, that.email) &&
                eventType == that.eventType &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, eventType, timestamp);
    }

    @Override
    public String toString() {
        return "UserEventDto{" +
                "email='" + email + '\'' +
                ", eventType=" + eventType +
                ", timestamp=" + timestamp +
                '}';
    }

    public enum EventType {
        USER_CREATED,
        USER_DELETED
    }
}
