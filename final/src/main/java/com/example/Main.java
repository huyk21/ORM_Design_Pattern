package com.example;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Create the MySQL connection factory using the default configuration
        MySQLConnectionFactory factory = MySQLConnectionFactory.createDefault(
                "localhost", // host
                "3306",      // port
                "damframework", // database
                "root",      // username
                "ducanh123"  // password
        );

        try {
            // Create a DatabaseSession using the factory
            DatabaseSession session = new DatabaseSession(factory);

            // Create a GenericDao for the User entity
            GenericDao<User> dao = new GenericDao<>(session, User.class);

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("Select an operation:");
                System.out.println("1. List all users");
                System.out.println("2. Add new user");
                System.out.println("3. Update user");
                System.out.println("4. Delete user");
                System.out.println("5. Select users with filters");
                System.out.println("6. Exit");

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        // List all users
                        List<User> users = dao.select(null, null, null);  // Get all users
                        if (users.isEmpty()) {
                            System.out.println("No users found.");
                        } else {
                            for (User user : users) {
                                System.out.println("ID: " + user.getId() + ", Username: " + user.getUsername());
                            }
                        }
                        break;

                    case "5":
                        // Select users with WHERE, GROUP BY, and HAVING
                        System.out.print("Enter WHERE clause (or press Enter to skip): ");
                        String where = scanner.nextLine();
                        System.out.print("Enter GROUP BY clause (or press Enter to skip): ");
                        String groupBy = scanner.nextLine();
                        System.out.print("Enter HAVING clause (or press Enter to skip): ");
                        String having = scanner.nextLine();

                        List<User> selectedUsers = dao.select(where.isEmpty() ? null : where, 
                                                               groupBy.isEmpty() ? null : groupBy, 
                                                               having.isEmpty() ? null : having);

                        if (selectedUsers.isEmpty()) {
                            System.out.println("No users found with the given filters.");
                        } else {
                            for (User user : selectedUsers) {
                                System.out.println("ID: " + user.getId() + ", Username: " + user.getUsername());
                            }
                        }
                        break;

                    case "6":
                        // Exit the program
                        session.closeConnection();
                        System.out.println("Exiting program.");
                        return;

                    // other cases...
                }
            }

        } catch (SQLException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }
}
