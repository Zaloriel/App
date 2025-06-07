package service.impl;

import models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.dto.CreateUserRequest;
import service.dto.UpdateUserRequest;
import service.dto.UserDto;
import service.exception.UserAlreadyExistsException;
import service.exception.UserNotFoundException;
import service.mapper.UserMapper;
import service.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDto testUserDto;
    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Тест Тестов");
        testUser.setEmail("test@example.com");
        testUser.setAge(25);
        testUser.setCreatedAt(LocalDateTime.now());

        testUserDto = new UserDto();
        testUserDto.setId(1L);
        testUserDto.setName("Тест Тестов");
        testUserDto.setEmail("test@example.com");
        testUserDto.setAge(25);
        testUserDto.setCreatedAt(testUser.getCreatedAt());

        createUserRequest = new CreateUserRequest("Новый Пользователь", "new@example.com", 30);
        updateUserRequest = new UpdateUserRequest("Обновленный Пользователь", "updated@example.com", 35);
    }

    @Test
    void shouldCreateUser() {
        // Given
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(userMapper.toEntity(createUserRequest)).thenReturn(testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When
        UserDto result = userService.createUser(createUserRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(testUserDto.getName());
        assertThat(result.getEmail()).isEqualTo(testUserDto.getEmail());

        verify(userRepository).existsByEmail(createUserRequest.getEmail());
        verify(userMapper).toEntity(createUserRequest);
        verify(userRepository).save(testUser);
        verify(userMapper).toDto(testUser);
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithExistingEmail() {
        // Given
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(createUserRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User with email " + createUserRequest.getEmail() + " already exists");

        verify(userRepository).existsByEmail(createUserRequest.getEmail());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toEntity(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void shouldGetUserById() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When
        UserDto result = userService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo(testUser.getName());

        verify(userRepository).findById(userId);
        verify(userMapper).toDto(testUser);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with ID " + userId + " not found");

        verify(userRepository).findById(userId);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void shouldGetUserByEmail() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When
        UserDto result = userService.getUserByEmail(email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);

        verify(userRepository).findByEmail(email);
        verify(userMapper).toDto(testUser);
    }

    @Test
    void shouldReturnNullWhenUserNotFoundByEmail() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        UserDto result = userService.getUserByEmail(email);

        // Then
        assertThat(result).isNull();

        verify(userRepository).findByEmail(email);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void shouldGetAllUsers() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setName("Второй Пользователь");
        user2.setEmail("second@example.com");

        UserDto userDto2 = new UserDto();
        userDto2.setId(2L);
        userDto2.setName("Второй Пользователь");
        userDto2.setEmail("second@example.com");

        List<User> users = Arrays.asList(testUser, user2);
        List<UserDto> userDtos = Arrays.asList(testUserDto, userDto2);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDtos(users)).thenReturn(userDtos);

        // When
        List<UserDto> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Тест Тестов");
        assertThat(result.get(1).getName()).isEqualTo("Второй Пользователь");

        verify(userRepository).findAll();
        verify(userMapper).toDtos(users);
    }

    @Test
    void shouldUpdateUser() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot(updateUserRequest.getEmail(), userId)).thenReturn(false);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When
        UserDto result = userService.updateUser(userId, updateUserRequest);

        // Then
        assertThat(result).isNotNull();

        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmailAndIdNot(updateUserRequest.getEmail(), userId);
        verify(userMapper).updateEntityFromRequest(updateUserRequest, testUser);
        verify(userRepository).save(testUser);
        verify(userMapper).toDto(testUser);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingUserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userId, updateUserRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with ID " + userId + " not found");

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).updateEntityFromRequest(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithExistingEmail() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot(updateUserRequest.getEmail(), userId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userId, updateUserRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User with email " + updateUserRequest.getEmail() + " already exists");

        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmailAndIdNot(updateUserRequest.getEmail(), userId);
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).updateEntityFromRequest(any(), any());
    }

    @Test
    void shouldUpdateUserWithSameEmail() {
        // Given
        Long userId = 1L;
        UpdateUserRequest sameEmailRequest = new UpdateUserRequest("Новое Имя", testUser.getEmail(), 30);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When
        UserDto result = userService.updateUser(userId, sameEmailRequest);

        // Then
        assertThat(result).isNotNull();

        verify(userRepository).findById(userId);
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), any());
        verify(userMapper).updateEntityFromRequest(sameEmailRequest, testUser);
        verify(userRepository).save(testUser);
        verify(userMapper).toDto(testUser);
    }

    @Test
    void shouldDeleteUser() {
        // Given
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Given
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with ID " + userId + " not found");

        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(any());
    }
}
