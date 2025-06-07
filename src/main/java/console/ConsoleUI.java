package console;

import org.springframework.stereotype.Component;
import service.UserService;
import service.dto.*;
import service.exception.*;

import java.util.List;
import java.util.Scanner;

@Component
public class ConsoleUI {

    private final UserService userService;
    private final Scanner scanner;

    public ConsoleUI(UserService userService) {
        this.userService = userService;
        this.scanner = new Scanner(System.in);
    }

    public void startInteractiveMode() {
        while (true) {
            printMenu();
            int choice = readInt("Выберите вариант: ");

            try {
                switch (choice) {
                    case 1 -> createUser();
                    case 2 -> getUserById();
                    case 3 -> getUserByEmail();
                    case 4 -> getAllUsers();
                    case 5 -> updateUser();
                    case 6 -> deleteUser();
                    case 0 -> { return; }
                    default -> System.out.println("Нет такого варанта!");
                }
            } catch (UserNotFoundException | UserAlreadyExistsException e) {
                System.err.println("Ошибка: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("СуперОшибка: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== Система взаимодействия с пользователем ===");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Найти по ID");
        System.out.println("3. Найти по Email");
        System.out.println("4. Вывести всех");
        System.out.println("5. Обновить пользователя");
        System.out.println("6. Удалить пользователя");
        System.out.println("0. Выход");
    }

    private void createUser() {
        System.out.println("\n--- Создание нового пользователя ---");
        String name = readLine("Имя: ");
        String email = readLine("Email: ");
        int age = readInt("Возраст: ");

        CreateUserRequest request = new CreateUserRequest(name, email, age);
        UserDto createdUser = userService.createUser(request);
        System.out.println("Успешно создан:\n" + formatUser(createdUser));
    }

    private void getUserById() {
        long id = readLong("ID Пользователя: ");
        UserDto user = userService.getUserById(id);
        if (user == null) {
            System.out.println("Не найден");
        } else {
            System.out.println("\nПользователь:\n" + formatUser(user));
        }
    }

    private void getUserByEmail() {
        String email = readLine("Email: ");
        UserDto user = userService.getUserByEmail(email);
        if (user == null) {
            System.out.println("Не найден");
        } else {
            System.out.println("\nПользователь:\n" + formatUser(user));
        }
    }

    private void getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("Не найдено");
            return;
        }

        System.out.println("\n--- Все пользователи (" + users.size() + ") ---");
        users.forEach(u -> System.out.println(formatUser(u)));
    }

    private void updateUser() {
        long id = readLong("ID Потзователя : ");
        UserDto current = userService.getUserById(id);

        System.out.println("\nИекущая информация:");
        System.out.println(formatUser(current));

        System.out.println("\nВвести новые значения (Оставить пустым для отмены):");
        String name = readLineOptional("Имя [" + current.getName() + "]: ", current.getName());
        String email = readLineOptional("Email [" + current.getEmail() + "]: ", current.getEmail());
        int age = readIntOptional("Возраст [" + current.getAge() + "]: ", current.getAge());

        UpdateUserRequest request = new UpdateUserRequest(name, email, age);
        UserDto updated = userService.updateUser(id, request);
        System.out.println("\nИнформация о пользователе обновлена:\n" + formatUser(updated));
    }

    private void deleteUser() {
        long id = readLong("ID Пользователя на удаление: ");
        System.out.print("Вы уверены? (y/n): ");
        if (scanner.nextLine().equalsIgnoreCase("y")) {
            userService.deleteUser(id);
            System.out.println("Пользователь успешно удален");
        } else {
            System.out.println("Удаление отменено");
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private String readLineOptional(String prompt, String defaultValue) {
        System.out.print(prompt);
        String input = scanner.nextLine();
        return input.isBlank() ? defaultValue : input;
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Введите действительное число");
            }
        }
    }

    private int readIntOptional(String prompt, int defaultValue) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine();
                return input.isBlank() ? defaultValue : Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Введите действительное число");
            }
        }
    }

    private long readLong(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Long.parseLong(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Введите действительное ID");
            }
        }
    }

    private String formatUser(UserDto user) {
        return String.format(
                "ID: %d\nИмя: %s\nEmail: %s\nВозраст: %d\nСоздан в: %s\n",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getCreatedAt()
        );
    }
}
