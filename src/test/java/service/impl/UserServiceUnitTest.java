package service.impl;

import dao.UserDao;
import models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceUnitTest {
    private AutoCloseable closeable;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    @DisplayName("Успешное создание пользователя с корректными данными")
    void shouldCreateUserSuccessfully() {
        // Given
        System.out.println("Test started");
        String name = "John Doe";
        String email = "john@example.com";
        Integer age = 25;

        User savedUser = new User(name, email, age);
        savedUser.setId(1L);

        when(userDao.findByEmail(email)).thenReturn(null);
        when(userDao.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.createUser(name, email, age);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals(25, result.getAge());

        verify(userDao).findByEmail(email);
        verify(userDao).save(any(User.class));
    }

    @Test
    @DisplayName("Ошибка при создании пользователя с существующим email")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        System.out.println("Test started");
        String name = "John Doe";
        String email = "john@example.com";
        Integer age = 25;

        User existingUser = new User("Jane Doe", email, 30);
        existingUser.setId(1L);

        when(userDao.findByEmail(email)).thenReturn(existingUser);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(name, email, age)
        );

        assertEquals("Пользователь с email " + email + " уже существует", exception.getMessage());
        verify(userDao).findByEmail(email);
        verify(userDao, never()).save(any(User.class));
    }


    @Test
    @DisplayName("Ошибка при создании пользователя с некорректными данными")
    void shouldThrowExceptionWhenInvalidData() {
        System.out.println("Test started");
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(null, "email@example.com", 25));
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("", "email@example.com", 25));
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("John", null, 25));
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("John", "", 25));

        verifyNoInteractions(userDao);
    }

    @Test
    @DisplayName("Успешное обновление данных пользователя")
    void shouldUpdateUserSuccessfully() {
        // Given
        System.out.println("Test started");
        Long userId = 123L;
        User existingUser = new User("John Doe", "john@example.com", 25);
        existingUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDao.findByEmail("john.smith@example.com")).thenReturn(null);

        when(userDao.update(any(User.class))).thenAnswer(invocation -> {
            return invocation.<User>getArgument(0);
        });

        // When
        User result = userService.updateUser(userId, "John Smith", "john.smith@example.com", 26);

        // Then
        assertNotNull(result);
        assertEquals("John Smith", result.getName());
        assertEquals("john.smith@example.com", result.getEmail());
        assertEquals(26, result.getAge());

        verify(userDao).findById(userId);
        verify(userDao).findByEmail("john.smith@example.com");
        verify(userDao).update(any(User.class));
    }
}
