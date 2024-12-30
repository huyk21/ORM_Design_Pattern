// File: Main.java
package com.example;

import java.sql.SQLException;
import java.util.List;

import com.example.client.User;
import com.example.connection.DatabaseSession;
import com.example.connection.MySQLConnectionFactory;

/**
 * Entry point for testing the specific SELECT query using Generic DAO implementation.
 */
public class Main {
    private static DatabaseSession session;

    public static void main(String[] args) {
        try {
            // Step 1: Create the MySQL connection factory with your database credentials
            MySQLConnectionFactory factory = MySQLConnectionFactory.createDefault(
                    "localhost", "3306", "damframework", "root", "ducanh123"
            );

            // Step 2: Initialize the database session using the connection factory
            session = new DatabaseSession(factory);

            // Step 3: Initialize Generic DAO for User using the refactored GenericDaoImpl
            Dao<User> userDao = new GenericDaoImpl<>(session, User.class);

            // Optional: Test database connection
            testDatabaseConnection(session);

            // Step 4: Begin transaction (optional for SELECT operations, but included for consistency)
            session.beginTransaction();

            // Step 5: Perform the specific SELECT query
            testSelectOperations(userDao);

            // Step 6: Commit transaction
            session.commitTransaction();
            System.out.println("Transaction committed successfully.");
        } catch (Exception e) {
            // Step 7: Rollback transaction on error
            System.err.println("Error occurred, rolling back transaction: " + e.getMessage());
            e.printStackTrace();
            try {
                if (session != null) {
                    session.rollbackTransaction();
                    System.out.println("Transaction rolled back successfully.");
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
                rollbackEx.printStackTrace();
            }
        } finally {
            // Step 8: Ensure the database connection is closed
            if (session != null) {
                testCloseConnection(session);
            }
        }
    }

    /**
     * Tests the database connection by checking if it's active.
     *
     * @param session The DatabaseSession instance.
     */
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

    /**
     * Executes the custom SELECT query involving JOIN and GROUP BY.
     *
     * @param userDao The DAO instance for User.
     */
    private static void testSelectOperations(Dao<User> userDao) {
        try {
            System.out.println("Testing select with JOIN and GROUP BY...");

            // Step 1: Build the query using SelectBuilder with JOIN and GROUP BY
            SelectBuilder<User> builder = new SelectBuilder<>(User.class)
                    .addScalar("MAX", "users.id", "max_user_id")
                    .addScalar("MIN", "users.id", "min_user_id")
                    .addColumn("users.username")
                    .addJoin("JOIN", "classes", "c", "users.class_id = c.id")
                    .groupBy("users.username");

            // Step 2: Generate the SQL query string
            String query = builder.buildSelectQuery();
            System.out.println("Generated Query: " + query);

            // Step 3: Execute the query using the DAO's select method
            List<Object[]> results = userDao.select(builder);

            // Step 4: Process and print the results
            System.out.println("Query Results:");
            for (Object[] row : results) {
                // Assuming the order is: max_user_id, min_user_id, username
                Long maxUserId = row[0] != null ? ((Number) row[0]).longValue() : null;
                Long minUserId = row[1] != null ? ((Number) row[1]).longValue() : null;
                String username = row[2] != null ? row[2].toString() : "N/A";

                System.out.println("Username: " + username + ", Max User ID: " + maxUserId + ", Min User ID: " + minUserId);
            }

        } catch (Exception e) {
            System.err.println("Select operation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tests closing the database connection.
     *
     * @param session The DatabaseSession instance.
     */
    private static void testCloseConnection(DatabaseSession session) {
        try {
            System.out.println("Closing database connection...");
            session.closeConnection();
            System.out.println("Database connection closed successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to close database connection: " + e.getMessage());
        }
    }
}
