package service.impl;

import dao.UserDao;
import dao.impl.UserDaoImpl;
import models.User;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("postgres");

    private UserServiceImpl userService;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        String jdbcUrl = postgres.getJdbcUrl();
        connection = DriverManager.getConnection(jdbcUrl, postgres.getUsername(), postgres.getPassword());

        String createTableSql = """
            CREATE TABLE IF NOT EXISTS "user" (
                id SERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                email VARCHAR(255) UNIQUE NOT NULL,
                age INTEGER,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSql);
        }

        UserDao userDao = new TestUserDaoImpl(connection);
        userService = new UserServiceImpl(userDao);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM \"user\"");
        }
        connection.close();
    }

    @Test
    @DisplayName("Должен обрабатывать полный жизненный цикл пользователя - создание, чтение, обновление, удаление")
    void shouldHandleCompleteUserLifecycle() {
        System.out.println("Тест начался");
        User createdUser = userService.createUser("Тестовый пользователь", "test@example.com", 30);

        assertNotNull(createdUser.getId());
        assertEquals("Тестовый пользователь", createdUser.getName());
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals(30, createdUser.getAge());
        assertNotNull(createdUser.getCreatedAt());

        Optional<User> foundById = userService.getUserById(createdUser.getId());
        assertTrue(foundById.isPresent());
        assertEquals(createdUser.getId(), foundById.get().getId());

        User foundByEmail = userService.getUserByEmail("test@example.com");
        assertNotNull(foundByEmail);
        assertEquals(createdUser.getId(), foundByEmail.getId());

        User updatedUser = userService.updateUser(createdUser.getId(), "Обновлённое имя", "updated@example.com", 31);
        assertEquals("Обновлённое имя", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals(31, updatedUser.getAge());

        boolean deleted = userService.deleteUser(createdUser.getId());
        assertTrue(deleted);

        Optional<User> deletedUser = userService.getUserById(createdUser.getId());
        assertFalse(deletedUser.isPresent());
    }

    @Test
    @DisplayName("Должен корректно обрабатывать несколько пользователей")
    void shouldHandleMultipleUsers() {
        System.out.println("Тест начался");
        User user1 = userService.createUser("Пользователь 1", "user1@example.com", 25);
        User user2 = userService.createUser("Пользователь 2", "user2@example.com", 30);
        User user3 = userService.createUser("Пользователь 3", "user3@example.com", 35);

        List<User> allUsers = userService.getAllUsers();
        assertEquals(3, allUsers.size());

        assertTrue(userService.getUserById(user1.getId()).isPresent());
        assertTrue(userService.getUserById(user2.getId()).isPresent());
        assertTrue(userService.getUserById(user3.getId()).isPresent());

        // Проверка уникальности email
        assertNotNull(userService.getUserByEmail("user1@example.com"));
        assertNotNull(userService.getUserByEmail("user2@example.com"));
        assertNotNull(userService.getUserByEmail("user3@example.com"));
    }

    @Test
    @DisplayName("Должен предотвращать создание пользователей с одинаковыми email")
    void shouldPreventDuplicateEmails() {
        // Создание первого пользователя
        System.out.println("Тест начался");
        userService.createUser("Первый пользователь", "duplicate@example.com", 25);

        // Попытка создать второго пользователя с таким же email
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("Второй пользователь", "duplicate@example.com", 30)
        );

        assertEquals("User with email duplicate@example.com already exists", exception.getMessage());
    }

    private static class TestUserDaoImpl implements UserDao {
        private final Connection connection;

        public TestUserDaoImpl(Connection connection) {
            this.connection = connection;
        }

        @Override
        public User save(User user) {
            String sql = "INSERT INTO \"user\" (name, email, age, created_at) VALUES (?, ?, ?, ?) RETURNING id, created_at";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, user.getName());
                stmt.setString(2, user.getEmail());
                stmt.setInt(3, user.getAge());
                stmt.setTimestamp(4, Timestamp.valueOf(user.getCreatedAt()));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        user.setId(rs.getLong("id"));
                        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Ошибка при создании пользователя", e);
            }

            return user;
        }

        @Override
        public Optional<User> findById(Long id) {
            String sql = "SELECT * FROM \"user\" WHERE id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, id);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Ошибка при поиске пользователя по id", e);
            }

            return Optional.empty();
        }

        @Override
        public User findByEmail(String email) {
            String sql = "SELECT * FROM \"user\" WHERE email = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, email);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Ошибка при поиске пользователя по email", e);
            }

            return null;
        }

        @Override
        public List<User> findAll() {
            List<User> users = new java.util.ArrayList<>();
            String sql = "SELECT * FROM \"user\" ORDER BY id";

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Ошибка при получении всех пользователей", e);
            }

            return users;
        }

        @Override
        public User update(User user) {
            String sql = "UPDATE \"user\" SET name = ?, email = ?, age = ? WHERE id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, user.getName());
                stmt.setString(2, user.getEmail());
                stmt.setInt(3, user.getAge());
                stmt.setLong(4, user.getId());

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new RuntimeException("Пользователь не найден с id: " + user.getId());
                }
            } catch (SQLException e) {
                throw new RuntimeException("Ошибка при обновлении пользователя", e);
            }

            return user;
        }

        @Override
        public void delete(User user) {
            String sql = "DELETE FROM \"user\" WHERE id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, user.getId());
                int affectedRows = stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Ошибка при удалении пользователя", e);
            }
        }

        @Override
        public boolean deleteById(Long id) {
            String sql = "DELETE FROM \"user\" WHERE id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, id);

                int rowsDeleted = stmt.executeUpdate();
                return rowsDeleted > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Ошибка при удалении пользователя", e);
            }
        }

        private User mapResultSetToUser(ResultSet rs) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            user.setAge(rs.getInt("age"));
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return user;
        }
    }
}