package service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на обновление данных пользователя")
public class UpdateUserRequest {

    @Schema(description = "Новое имя пользователя", example = "Иван Петров")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Schema(description = "Новый email адрес пользователя", example = "ivan.petrov@example.com")
    @Email(message = "Email should be valid")
    private String email;

    @Schema(description = "Новый возраст пользователя", example = "30", minimum = "0")
    @Min(value = 0, message = "Age must be non-negative")
    private Integer age;

    public UpdateUserRequest() {}

    public UpdateUserRequest(String name, String email, Integer age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
