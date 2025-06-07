package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import service.UserService;
import service.dto.*;
import service.exception.UserAlreadyExistsException;
import service.exception.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = {UserController.class, TestConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto testUser;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new UserDto(1L, "John Doe", "john@example.com", 30, LocalDateTime.now());
        createRequest = new CreateUserRequest("John Doe", "john@example.com", 30);
        updateRequest = new UpdateUserRequest("John Updated", "john.updated@example.com", 31);


        reset(userService);
    }

    @Test
    void createUser_shouldReturn201AndUserDto_whenValidRequest() throws Exception {
        // Given
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    void createUser_shouldReturn400_whenInvalidRequest() throws Exception {
        // Given - invalid request with empty name and invalid email
        CreateUserRequest invalidRequest = new CreateUserRequest("", "invalid-email", -5);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.age").exists());

        verify(userService, never()).createUser(any(CreateUserRequest.class));
    }

    @Test
    void createUser_shouldReturn409_whenUserAlreadyExists() throws Exception {
        // Given
        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User with email john@example.com already exists"));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("User with email john@example.com already exists"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    void getUserById_shouldReturn200AndUserDto_whenUserExists() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUser);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.age").value(30));

        verify(userService).getUserById(1L);
    }

    @Test
    void getUserById_shouldReturn404_whenUserNotFound() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new UserNotFoundException("User with ID 999 not found"));

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User with ID 999 not found"));

        verify(userService).getUserById(999L);
    }

    @Test
    void getAllUsers_shouldReturn200AndUserList() throws Exception {
        List<UserDto> users = Arrays.asList(
                testUser,
                new UserDto(2L, "Jane Smith", "jane@example.com", 25, LocalDateTime.now())
        );
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));

        verify(userService).getAllUsers();
    }

    @Test
    void updateUser_shouldReturn200AndUpdatedUser_whenValidRequest() throws Exception {
        UserDto updatedUser = new UserDto(1L, "John Updated", "john.updated@example.com", 31, LocalDateTime.now());
        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"))
                .andExpect(jsonPath("$.age").value(31));

        verify(userService).updateUser(eq(1L), any(UpdateUserRequest.class));
    }

    @Test
    void deleteUser_shouldReturn204_whenUserExists() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_shouldReturn404_whenUserNotFound() throws Exception {
        doThrow(new UserNotFoundException("User with ID 999 not found"))
                .when(userService).deleteUser(999L);

        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User with ID 999 not found"));

        verify(userService).deleteUser(999L);
    }
}