package service.impl;

import dao.UserDao;
import dao.impl.UserDaoImpl;
import models.User;
import service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;


public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserDao userDao;

    public UserServiceImpl() {
        this.userDao = new UserDaoImpl();
    }

    @Override
    public User createUser(String name, String email, Integer age) {
        logger.debug("Creating user with name: {}, email: {}, age: {}", name, email, age);

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("User email cannot be empty");
        }

        User existingUser = userDao.findByEmail(email);
        if (existingUser != null) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }

        User user = new User(name, email, age);
        return userDao.save(user);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        logger.debug("Getting user by id: {}", id);
        return userDao.findById(id);
    }

    @Override
    public User getUserByEmail(String email) {
        logger.debug("Getting user by email: {}", email);
        return userDao.findByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        logger.debug("Getting all users");
        return userDao.findAll();
    }

    @Override
    public User updateUser(Long id, String name, String email, Integer age) {
        logger.debug("Updating user with id: {}", id);

        Optional<User> userOptional = userDao.findById(id);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User with id " + id + " not found");
        }

        User user = userOptional.get();

        if (name != null && !name.trim().isEmpty()) {
            user.setName(name);
        }

        if (email != null && !email.trim().isEmpty() && !email.equals(user.getEmail())) {
            User existingUser = userDao.findByEmail(email);
            if (existingUser != null && !existingUser.getId().equals(id)) {
                throw new IllegalArgumentException("User with email " + email + " already exists");
            }
            user.setEmail(email);
        }

        if (age != null) {
            user.setAge(age);
        }

        return userDao.update(user);
    }

    @Override
    public boolean deleteUser(Long id) {
        logger.debug("Deleting user with id: {}", id);
        return userDao.deleteById(id);
    }
}
