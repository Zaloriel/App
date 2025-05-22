
import models.User;
import service.UserService;
import service.impl.UserServiceImpl;
import util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;


public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static final UserService userService = new UserServiceImpl();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        logger.info("Starting User Service Application");

        try {
            boolean exit = false;
            while (!exit) {
                printMenu();
                int choice = readIntInput("Enter your choice: ");

                switch (choice) {
                    case 1:
                        createUser();
                        break;
                    case 2:
                        getUserById();
                        break;
                    case 3:
                        getUserByEmail();
                        break;
                    case 4:
                        getAllUsers();
                        break;
                    case 5:
                        updateUser();
                        break;
                    case 6:
                        deleteUser();
                        break;
                    case 0:
                        exit = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }

                if (!exit) {
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            System.out.println("An unexpected error occurred: " + e.getMessage());
        } finally {
            scanner.close();
            HibernateUtil.shutdown();
            logger.info("Application shutdown complete");
        }
    }

    private static void printMenu() {
        System.out.println("\n=== USER SERVICE MENU ===");
        System.out.println("1. Create new user");
        System.out.println("2. Get user by ID");
        System.out.println("3. Get user by email");
        System.out.println("4. Get all users");
        System.out.println("5. Update user");
        System.out.println("6. Delete user");
        System.out.println("0. Exit");
        System.out.println("========================");
    }

    private static void createUser() {
        System.out.println("\n=== CREATE NEW USER ===");

        String name = readStringInput("Enter name: ");
        String email = readStringInput("Enter email: ");
        Integer age = readIntInput("Enter age: ");

        try {
            User user = userService.createUser(name, email, age);
            System.out.println("User created successfully:");
            System.out.println(user);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            System.out.println("Error occurred while creating user: " + e.getMessage());
        }
    }

    private static void getUserById() {
        System.out.println("\n=== GET USER BY ID ===");

        Long id = readLongInput();

        try {
            Optional<User> userOptional = userService.getUserById(id);
            if (userOptional.isPresent()) {
                System.out.println("User found:");
                System.out.println(userOptional.get());
            } else {
                System.out.println("User with ID " + id + " not found.");
            }
        } catch (Exception e) {
            logger.error("Error getting user by ID: {}", e.getMessage(), e);
            System.out.println("Error occurred while getting user: " + e.getMessage());
        }
    }

    private static void getUserByEmail() {
        System.out.println("\n=== GET USER BY EMAIL ===");

        String email = readStringInput("Enter user email: ");

        try {
            User user = userService.getUserByEmail(email);
            if (user != null) {
                System.out.println("User found:");
                System.out.println(user);
            } else {
                System.out.println("User with email " + email + " not found.");
            }
        } catch (Exception e) {
            logger.error("Error getting user by email: {}", e.getMessage(), e);
            System.out.println("Error occurred while getting user: " + e.getMessage());
        }
    }

    private static void getAllUsers() {
        System.out.println("\n=== ALL USERS ===");

        try {
            List<User> users = userService.getAllUsers();
            if (users.isEmpty()) {
                System.out.println("No users found.");
            } else {
                System.out.println("Total users: " + users.size());
                for (User user : users) {
                    System.out.println(user);
                }
            }
        } catch (Exception e) {
            logger.error("Error getting all users: {}", e.getMessage(), e);
            System.out.println("Error occurred while getting users: " + e.getMessage());
        }
    }

    private static void updateUser() {
        System.out.println("\n=== UPDATE USER ===");

        Long id = readLongInput();

        try {
            Optional<User> userOptional = userService.getUserById(id);
            if (userOptional.isEmpty()) {
                System.out.println("User with ID " + id + " not found.");
                return;
            }

            User user = userOptional.get();
            System.out.println("Current user details:");
            System.out.println(user);

            System.out.println("\nEnter new values (leave empty to keep current value):");

            String name = readStringInputOptional("Enter new name [" + user.getName() + "]: ");
            String email = readStringInputOptional("Enter new email [" + user.getEmail() + "]: ");

            String ageInput = readStringInputOptional("Enter new age [" + user.getAge() + "]: ");
            Integer age = ageInput.isEmpty() ? null : Integer.parseInt(ageInput);

            user = userService.updateUser(id, name, email, age);
            System.out.println("User updated successfully:");
            System.out.println(user);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage(), e);
            System.out.println("Error occurred while updating user: " + e.getMessage());
        }
    }

    private static void deleteUser() {
        System.out.println("\n=== DELETE USER ===");

        Long id = readLongInput();

        try {
            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                System.out.println("User with ID " + id + " deleted successfully.");
            } else {
                System.out.println("User with ID " + id + " not found.");
            }
        } catch (Exception e) {
            logger.error("Error deleting user: {}", e.getMessage(), e);
            System.out.println("Error occurred while deleting user: " + e.getMessage());
        }
    }

    private static String readStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static String readStringInputOptional(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static Integer readIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    private static Long readLongInput() {
        while (true) {
            try {
                System.out.print("Enter user ID: ");
                String input = scanner.nextLine().trim();
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }
}
