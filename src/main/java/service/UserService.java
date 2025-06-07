package service;

import java.util.List;
import service.dto.CreateUserRequest;
import service.dto.UpdateUserRequest;
import service.dto.UserDto;


public interface UserService {

    UserDto createUser(CreateUserRequest request);

    UserDto getUserById(Long id);

    UserDto getUserByEmail(String email);

    List<UserDto> getAllUsers();

    UserDto updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);
}
