package com.example;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.example.GenericDao.SelectBuilder;
import com.example.client.User;
import com.example.connection.DatabaseSession;
import com.example.connection.MySQLConnectionFactory;

public class Main {
    private static DatabaseSession session;

    public static void main(String[] args) throws SQLException {
        // Create the MySQL connection factory
        MySQLConnectionFactory factory = MySQLConnectionFactory.createDefault(
                "localhost", "3306", "damframework", "root", "ducanh123"
        );

        // Initialize the database session
        session = new DatabaseSession(factory);

        // Initialize Generic DAO for User
        GenericDao<User> userDao = new GenericDao<>(session, User.class);

        try {
            
            testSelectOperations(userDao);
        } finally {
            // Ensure the database connection is closed
            testCloseConnection(session);
        }
    }

    private static void testDatabaseConnection(DatabaseSession session) {
        try {
            System.out.println("Testing database connection...");
            if (session.getConnection() != null && !session.getConnection().isClosed()) {
                System.out.println("Database connection is active.");
            } else {
                System.err.println("Failed to establish database connection.");
            }
        } catch (SQLException e) {
            System.err.println("Error during connection test: " + e.getMessage());
        }
    }

    private static void testInsertOperation(GenericDao<User> userDao) {
        try {
            System.out.println("Testing insert operation...");

            // Create a new User
            User newUser = new User();
            newUser.setUsername("test_user");
            newUser.setPassword("password123");
            newUser.setFullName("Test User");
            newUser.setEmail("testuser@example.com");
            newUser.setActive(true);

            // Insert the user into the database
            userDao.create(newUser);
            System.out.println("Insert successful: " + newUser);
        } catch (Exception e) {
            System.err.println("Insert operation failed: " + e.getMessage());
        }
    }

    private static void testUpdateOperation(GenericDao<User> userDao) {
        try {
            System.out.println("Testing update operation...");

            // Find a user by ID
            Optional<User> userOptional = userDao.findById(13); // Assume user with ID=1 exists
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setFullName("Updated User Name");

                // Update the user in the database
                userDao.update(user, "id = " + user.getId());
                System.out.println("Update successful: " + user);
            } else {
                System.err.println("User with ID=1 not found for update.");
            }
        } catch (Exception e) {
            System.err.println("Update operation failed: " + e.getMessage());
        }
    }

    private static void testDeleteOperation(GenericDao<User> userDao) {
        try {
            System.out.println("Testing delete operation...");

            // Delete user by condition
            userDao.delete("username = 'test_user'");
            System.out.println("Delete operation successful.");
        } catch (SQLException e) {
            System.err.println("Delete operation failed: " + e.getMessage());
        }
    }

    private static void testSelectOperations(GenericDao<User> userDao) {
        try {
            System.out.println("Testing select with JOIN and GROUP BY...");
    
            // Use SelectBuilder for a complex query with JOIN
            SelectBuilder builder = userDao.dynamicJoinBuilder();
            String query = builder
                    .addScalar("COUNT", "users.id", "user_count")      // Count users
                    .addColumn("users.username")                      // Grouping column
                  
                    .addJoin(com.example.client.Class.class, "classes", "users.class_id = classes.id") // Use fully qualified Class entity
                    .groupBy("users.username")                       // Group by username
                    .having("COUNT(users.id) > 1")                   // Having condition
                    .buildSelectQuery();
    
            System.out.println("Generated Query: " + query);
    
            // Execute the query
            List<Object[]> results = session.executeCustomJoinQuery(query);
            for (Object[] row : results) {
                System.out.println("User Count: " + row[0] + ", Username: " + row[1]);
            }
        } catch (Exception e) {
            System.err.println("Select operation failed: " + e.getMessage());
            e.printStackTrace();
        }
        
    }
    

    private static void testCloseConnection(DatabaseSession session) {
        try {
            System.out.println("Testing closing database connection...");
            session.closeConnection();
            System.out.println("Database connection closed successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to close database connection: " + e.getMessage());
        }
    }
}