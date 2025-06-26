package controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import service.UserService;
import service.dto.CreateUserRequest;
import service.dto.UpdateUserRequest;
import service.dto.UserDto;
import service.exception.UserNotFoundException;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/users")
@Validated
@Tag(name = "User Management", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(
            summary = "Создать нового пользователя",
            description = "Создает нового пользователя с указанными данными. Email должен быть уникальным."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно создан",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные запроса",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Пользователь с таким email уже существует",
                    content = @Content
            )
    })
    public ResponseEntity<EntityModel<UserDto>> createUser(
            @Parameter(description = "Данные для создания пользователя", required = true)
            @Valid @RequestBody CreateUserRequest request) {

        UserDto createdUser = userService.createUser(request);
        EntityModel<UserDto> userModel = EntityModel.of(createdUser)
                .add(linkTo(methodOn(UserController.class).getUserById(createdUser.getId())).withSelfRel())
                .add(linkTo(methodOn(UserController.class).updateUser(createdUser.getId(), null)).withRel("update"))
                .add(linkTo(methodOn(UserController.class).deleteUser(createdUser.getId())).withRel("delete"))
                .add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));

        return ResponseEntity.status(HttpStatus.CREATED).body(userModel);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает данные пользователя по указанному идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content
            )
    })
    public ResponseEntity<EntityModel<UserDto>> getUserById(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long id) {

        UserDto user = userService.getUserById(id);
        EntityModel<UserDto> userModel = EntityModel.of(user)
                .add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel())
                .add(linkTo(methodOn(UserController.class).updateUser(id, null)).withRel("update"))
                .add(linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"))
                .add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));

        return ResponseEntity.ok(userModel);
    }

    @GetMapping("/email/{email}")
    @Operation(
            summary = "Получить пользователя по email",
            description = "Возвращает данные пользователя по указанному email адресу"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь с указанным email не найден",
                    content = @Content
            )
    })
    public ResponseEntity<EntityModel<UserDto>> getUserByEmail(
            @Parameter(description = "Email пользователя", required = true, example = "user@example.com")
            @PathVariable String email) {

        UserDto user = userService.getUserByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User with email " + email + " not found");
        }

        EntityModel<UserDto> userModel = EntityModel.of(user)
                .add(linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel())
                .add(linkTo(methodOn(UserController.class).getUserByEmail(email)).withRel("by-email"))
                .add(linkTo(methodOn(UserController.class).updateUser(user.getId(), null)).withRel("update"))
                .add(linkTo(methodOn(UserController.class).deleteUser(user.getId())).withRel("delete"))
                .add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));

        return ResponseEntity.ok(userModel);
    }

    @GetMapping
    @Operation(
            summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей в системе"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список пользователей успешно получен",
                    content = @Content(schema = @Schema(implementation = CollectionModel.class))
            )
    })
    public ResponseEntity<CollectionModel<EntityModel<UserDto>>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();

        List<EntityModel<UserDto>> userModels = users.stream()
                .map(user -> EntityModel.of(user)
                        .add(linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel())
                        .add(linkTo(methodOn(UserController.class).updateUser(user.getId(), null)).withRel("update"))
                        .add(linkTo(methodOn(UserController.class).deleteUser(user.getId())).withRel("delete")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<UserDto>> collectionModel = CollectionModel.of(userModels)
                .add(linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel())
                .add(linkTo(methodOn(UserController.class).createUser(null)).withRel("create"));

        return ResponseEntity.ok(collectionModel);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить пользователя",
            description = "Обновляет данные существующего пользователя по указанному ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно обновлен",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные запроса",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Пользователь с таким email уже существует",
                    content = @Content
            )
    })
    public ResponseEntity<EntityModel<UserDto>> updateUser(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Данные для обновления пользователя", required = true)
            @Valid @RequestBody UpdateUserRequest request) {

        UserDto updatedUser = userService.updateUser(id, request);
        EntityModel<UserDto> userModel = EntityModel.of(updatedUser)
                .add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel())
                .add(linkTo(methodOn(UserController.class).updateUser(id, null)).withRel("update"))
                .add(linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"))
                .add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));

        return ResponseEntity.ok(userModel);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить пользователя",
            description = "Удаляет пользователя по указанному ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Пользователь успешно удален"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long id) {

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}