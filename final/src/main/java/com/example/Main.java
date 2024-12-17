package com.example;

import com.example.client.User;
import com.example.connection.ConnectionFactory;
import com.example.connection.DatabaseSession;
import com.example.connection.MySQLConnectionFactory;
import com.example.entity.GenericDao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Create the MySQL connection factory using the default configuration
        ConnectionFactory factory = MySQLConnectionFactory.createDefault(
                "localhost", // host
                "3306",      // port
                "ORMX",      // database
                "root",      // username
                "mysql"      // password
        );

        try (Scanner scanner = new Scanner(System.in)) {
            // Create a DatabaseSession using the factory
            DatabaseSession session = new DatabaseSession(factory);

            // Create a GenericDao for the User entity
            GenericDao<User> dao = new GenericDao<>(session, User.class);

            while (true) {
                System.out.println("\n=== User Management System ===");
                System.out.println("1. List all users");
                System.out.println("2. Add new user");
                System.out.println("3. Update user");
                System.out.println("4. Delete user");
                System.out.println("5. Select users with filters");
                System.out.println("6. Find user by ID");
                System.out.println("7. Exit");
                System.out.print("Enter your choice: ");

                String choice = scanner.nextLine();

                try {
                    switch (choice) {
                        case "1":
                            // List all users
                            List<User> users = dao.findAll();
                            if (users.isEmpty()) {
                                System.out.println("No users found.");
                            } else {
                                System.out.println("\nUser List:");
                                users.forEach(user -> System.out.printf("ID: %d, Username: %s%n",
                                        user.getId(), user.getUsername()));
                            }
                            break;

                        case "2":
                            // Add new user
                            System.out.print("Enter username: ");
                            String username = scanner.nextLine();
                            System.out.print("Enter password: ");
                            String password = scanner.nextLine();

                            User newUser = new User();
                            newUser.setUsername(username);
                            newUser.setPassword(password);
                            dao.save(newUser);
                            System.out.println("User added successfully!");
                            break;

                        case "3":
                            // Update user
                            System.out.print("Enter user ID to update: ");
                            int updateId = Integer.parseInt(scanner.nextLine());
                            Optional<User> updateUser = dao.findById(updateId);

                            if (updateUser.isPresent()) {
                                User user = updateUser.get();
                                System.out.print("Enter new username (or press Enter to skip): ");
                                String newUsername = scanner.nextLine();
                                System.out.print("Enter new password (or press Enter to skip): ");
                                String newPassword = scanner.nextLine();

                                if (!newUsername.isEmpty()) user.setUsername(newUsername);
                                if (!newPassword.isEmpty()) user.setPassword(newPassword);

                                dao.update(user);
                                System.out.println("User updated successfully!");
                            } else {
                                System.out.println("User not found!");
                            }
                            break;

                        case "4":
                            // Delete user
                            System.out.print("Enter user ID to delete: ");
                            int deleteId = Integer.parseInt(scanner.nextLine());
                            dao.delete(deleteId);
                            System.out.println("User deleted successfully!");
                            break;

                        case "5":
                            // Select users with WHERE, GROUP BY, and HAVING
                            System.out.print("Enter WHERE clause (or press Enter to skip): ");
                            String where = scanner.nextLine();
                            System.out.print("Enter GROUP BY clause (or press Enter to skip): ");
                            String groupBy = scanner.nextLine();
                            System.out.print("Enter HAVING clause (or press Enter to skip): ");
                            String having = scanner.nextLine();

                            List<User> selectedUsers = dao.select(
                                    where.isEmpty() ? null : where,
                                    groupBy.isEmpty() ? null : groupBy,
                                    having.isEmpty() ? null : having
                            );

                            if (selectedUsers.isEmpty()) {
                                System.out.println("No users found with the given filters.");
                            } else {
                                selectedUsers.forEach(user -> System.out.printf("ID: %d, Username: %s%n",
                                        user.getId(), user.getUsername()));
                            }
                            break;

                        case "6":
                            // Find user by ID
                            System.out.print("Enter user ID: ");
                            int searchId = Integer.parseInt(scanner.nextLine());
                            Optional<User> foundUser = dao.findById(searchId);

                            foundUser.ifPresentOrElse(
                                    user -> System.out.printf("Found user - ID: %d, Username: %s%n",
                                            user.getId(), user.getUsername()),
                                    () -> System.out.println("User not found!")
                            );
                            break;

                        case "7":
                            // Exit the program
                            System.out.println("Closing connection and exiting...");
                            session.closeConnection();
                            return;

                        default:
                            System.out.println("Invalid choice! Please try again.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format! Please enter a valid number.");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}