package service.impl;

import models.User;
import service.UserService;
import service.dto.CreateUserRequest;
import service.dto.UpdateUserRequest;
import service.dto.UserDto;
import service.exception.UserAlreadyExistsException;
import service.exception.UserNotFoundException;
import service.mapper.UserMapper;
import service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto createUser(CreateUserRequest request) {
        logger.debug("Creating user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);

        logger.info("User created successfully with ID: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        logger.debug("Getting user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));

        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        logger.debug("Getting user by email: {}", email);

        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.map(userMapper::toDto).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        logger.debug("Getting all users");

        List<User> users = userRepository.findAll();
        return userMapper.toDtos(users);
    }

    @Override
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        logger.debug("Updating user with ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));

        // Check if email is being changed and if new email already exists
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
        logger.debug("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }

        userRepository.deleteById(id);
        logger.info("User deleted successfully with ID: {}", id);
    }
}
