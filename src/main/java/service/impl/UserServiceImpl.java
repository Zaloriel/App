package service.impl;

import models.User;
import org.springframework.kafka.core.KafkaTemplate;
import service.UserService;
import service.dto.CreateUserRequest;
import service.dto.UpdateUserRequest;
import service.dto.UserDto;
import service.dto.UserEventDto;
import service.exception.UserAlreadyExistsException;
import service.exception.UserNotFoundException;
import service.mapper.UserMapper;
import service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KafkaTemplate<String, UserEventDto> kafkaTemplate;
    private final String userEventsTopic = "user-events";

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper,
                           KafkaTemplate<String, UserEventDto> kafkaTemplate) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public UserDto createUser(CreateUserRequest request) {
        logger.debug("Создание пользователя с email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Пользоваетель с email " + request.getEmail() + " уже существует");
        }

        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        sendUserEvent(savedUser.getEmail(), UserEventDto.EventType.USER_CREATED);

        logger.info("Пользователь успешно создан с ID: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        logger.debug("Найти юзера по ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Юзер с ID " + id + " не найден"));

        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        logger.debug("Найти юзера по email: {}", email);

        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.map(userMapper::toDto).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        logger.debug("Получение всех юзеров");

        List<User> users = userRepository.findAll();
        return userMapper.toDtos(users);
    }

    @Override
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        logger.debug("Updating user with ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));

        if (request.getEmail() != null && !request.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
            }
        }

        userMapper.updateEntityFromRequest(request, existingUser);
        User updatedUser = userRepository.save(existingUser);

        logger.info("User updated successfully with ID: {}", updatedUser.getId());
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        logger.debug("Удаление юзера с ID: {}", id);
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("Юзер с ID " + id + " не найден"));
            String userEmail = user.getEmail();
            userRepository.deleteById(id);
            sendUserEvent(userEmail, UserEventDto.EventType.USER_DELETED);

            logger.info("Юзер успешно удален с ID: {}", id);
        } catch (Exception e) {
            logger.error("Ошибка при удалении пользователя с ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Не удалось удалить пользователя", e);
        }
    }

    private void sendUserEvent(String email, UserEventDto.EventType eventType) {
        try {
            UserEventDto userEvent = new UserEventDto(email, eventType, LocalDateTime.now());

            kafkaTemplate.send(userEventsTopic, userEvent)
                    .whenComplete((result, throwable) -> {
                        if (throwable == null) {
                            logger.info("Событие {} для пользователя {} успешно отправлено в Kafka",
                                    eventType, email);
                        } else {
                            logger.error("Ошибка при отправке события {} для пользователя {} в Kafka: {}",
                                    eventType, email, throwable.getMessage(), throwable);
                        }
                    });

        } catch (Exception e) {
            logger.error("Не удалось отправить событие {} для пользователя {} в Kafka: {}",
                    eventType, email, e.getMessage(), e);
        }
    }
}