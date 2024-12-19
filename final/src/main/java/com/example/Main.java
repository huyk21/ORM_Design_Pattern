package com.example;

import com.example.client.User;
import com.example.connection.DatabaseSession;
import com.example.connection.MySQLConnectionFactory;
import com.example.entity.GenericDao;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // Create the MySQL connection factory using the default configuration
        MySQLConnectionFactory factory = MySQLConnectionFactory.createDefault(
                "localhost", "3306", "ORMX", "root", "mysql");

        try (Scanner scanner = new Scanner(System.in)) {
            // Create a DatabaseSession using the factory
            DatabaseSession session = new DatabaseSession(factory);

            // Create a GenericDao for the User entity
            GenericDao<User> dao = new GenericDao<>(session, User.class);

            while (true) {
                System.out.println("\n=== User Management System ===");
                System.out.println("1. List all users (SELECT)");
                System.out.println("2. Add new user (INSERT)");
                System.out.println("3. Update user (UPDATE)");
                System.out.println("4. Delete user (DELETE)");
                System.out.println("5. Select users with filters (WHERE, GROUP BY, HAVING)");
                System.out.println("6. Find user by ID (SELECT)");
                System.out.println("7. Exit");
                System.out.print("Enter your choice: ");

                String choice = scanner.nextLine();

                try {
                    switch (choice) {
                        case "1":
                            // List all users
                            // Example: Select Users with id and COUNT(id), GROUP BY id, HAVING COUNT(id) >
                            // 0
                            GenericDao.SelectBuilder builder = new GenericDao.SelectBuilder(User.class)
                                    .addColumn("id")
                                    .addColumn("username")
                                    .addColumn("email"); // Add columns you want to select

                            // Execute the query and get the results as a list of Objects (e.g., Integer)
                            List<Object> groupedResults = dao.select(builder);

                            // Print the scalar result (count of ids)
                            for (Object result : groupedResults) {
                                User user = (User) result; // Cast to User entity
                                System.out.println("User ID: " + user.getId() + ", Username: " + user.getUsername());
                            }
                            break;

                        case "2":
                            // Add new user
                            User newUser = new User();

                            System.out.print("Enter username: ");
                            String username = scanner.nextLine();
                            newUser.setUsername(username);

                            System.out.print("Enter password: ");
                            String password = scanner.nextLine();
                            newUser.setPassword(password);

                            System.out.print("Enter email (or press Enter to skip): ");
                            String email = scanner.nextLine();
                            if (!email.isEmpty())
                                newUser.setEmail(email);

                            System.out.print("Enter full name (or press Enter to skip): ");
                            String fullName = scanner.nextLine();
                            if (!fullName.isEmpty())
                                newUser.setFullName(fullName);

                            System.out.print("Enter date of birth (YYYY-MM-DD) (or press Enter to skip): ");
                            String dobStr = scanner.nextLine();
                            if (!dobStr.isEmpty()) {
                                try {
                                    Date dob = Date.valueOf(dobStr);
                                    newUser.setDateOfBirth(dob);
                                } catch (IllegalArgumentException e) {
                                    System.out.println("Invalid date format. Skipping date of birth.");
                                }
                            }

                            newUser.setActive(true);
                            newUser.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                            newUser.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

                            dao.create(newUser);
                            System.out.println("Query: " + dao.previewInsertQuery(newUser));
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
                                if (!newUsername.isEmpty())
                                    user.setUsername(newUsername);

                                System.out.print("Enter new password (or press Enter to skip): ");
                                String newPassword = scanner.nextLine();
                                if (!newPassword.isEmpty())
                                    user.setPassword(newPassword);

                                System.out.print("Enter new email (or press Enter to skip): ");
                                String newEmail = scanner.nextLine();
                                if (!newEmail.isEmpty())
                                    user.setEmail(newEmail);

                                System.out.print("Enter new full name (or press Enter to skip): ");
                                String newFullName = scanner.nextLine();
                                if (!newFullName.isEmpty())
                                    user.setFullName(newFullName);

                                System.out.print("Enter new date of birth (YYYY-MM-DD) (or press Enter to skip): ");
                                String newDobStr = scanner.nextLine();
                                if (!newDobStr.isEmpty()) {
                                    try {
                                        Date newDob = Date.valueOf(newDobStr);
                                        user.setDateOfBirth(newDob);
                                    } catch (IllegalArgumentException e) {
                                        System.out.println("Invalid date format. Skipping date of birth update.");
                                    }
                                }

                                System.out.print("Update active status? (y/n): ");
                                String activeChoice = scanner.nextLine();
                                if (activeChoice.equalsIgnoreCase("y")) {
                                    System.out.print("Set active (true/false): ");
                                    boolean isActive = Boolean.parseBoolean(scanner.nextLine());
                                    user.setActive(isActive);
                                }

                                user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

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
                                    having.isEmpty() ? null : having);

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
                                    () -> System.out.println("User not found!"));
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
