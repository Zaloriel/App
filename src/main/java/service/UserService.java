package service;

import models.User;
import java.util.List;
import java.util.Optional;


public interface UserService {


    User createUser(String name, String email, Integer age);

    Optional<User> getUserById(Long id);

    User getUserByEmail(String email);

    List<User> getAllUsers();


    User updateUser(Long id, String name, String email, Integer age);

    boolean deleteUser(Long id);
}
