package service.mapper;

import io.swagger.v3.oas.annotations.media.Schema;
import service.dto.CreateUserRequest;
import service.dto.UpdateUserRequest;
import service.dto.UserDto;
import models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {
    @Schema(description = "Преобразование User в UserDto")
    UserDto toDto(User user);

    @Schema(description = "Преобразование списка User в список UserDto")
    List<UserDto> toDtos(List<User> users);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Schema(description = "Создание User из CreateUserRequest")
    User toEntity(CreateUserRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Schema(description = "Обновление User из UpdateUserRequest")
    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User user);
}

