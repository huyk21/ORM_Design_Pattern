// File: Main.java
package com.example;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.example.annotation.validator.NotNull;
import com.example.cache.CachedDao;
import com.example.cache.EvictionPolicy;
import com.example.cache.LRUEvictionPolicy;
import com.example.client.User;
import com.example.connection.DatabaseSession;
import com.example.connection.MySQLConnectionFactory;
import com.example.validator.AlphanumericValidator;
import com.example.validator.ValidationProcessor;

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

            testValidatorFunction();

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


    private static void testCache(Dao<User> userDao) throws SQLException, ReflectiveOperationException {
    System.out.println("Testing Cache...");
// Create an eviction policy (e.g., LRU with a max size of 3)
    EvictionPolicy<Integer, User> evictionPolicy = new LRUEvictionPolicy<>(3);

    // Wrap the userDao with CachedDao
    CachedDao<User> cachedUserDao = new CachedDao<>(userDao, evictionPolicy); // Cache size = 3

    // Fetch user with ID 1 (first time, fetch from DB)
    Optional<User> user1 = cachedUserDao.findById(13);
    user1.ifPresent(user -> System.out.println("User fetched: " + user.getUsername()));

    // Fetch user with ID 1 again (should hit cache)
    Optional<User> cachedUser1 = cachedUserDao.findById(13);
    cachedUser1.ifPresent(user -> System.out.println("User fetched from cache: " + user.getUsername()));

   
}


private static void testValidatorFunction() {
        System.out.println("Testing Validator Functionality...");

        // Step 1: Create a test entity
        User user = new User();
        user.setUsername("abc"); // Invalid: not alphanumeric
        
        user.setEmail(null); // Invalid: null value

        // Step 2: Set up ValidationProcessor and register validators
        ValidationProcessor<User> processor = new ValidationProcessor<>();

        // Register built-in validators
        processor.registerValidator(NotNull.class, new AlphanumericValidator<>());
       

       

        // Step 3: Validate the entity
        boolean isValid = processor.validate(user);

        // Step 4: Output the validation results
        if (isValid) {
            System.out.println("Validation passed!");
        } else {
            System.out.println("Validation failed. See error messages above.");
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
     private static void testCreateOperation(Dao<User> userDao) {
        System.out.println("Testing Create Operation...");
        try {
            User user = new User();
            user.setUsername("john_doe");
            user.setEmail("john.doe@example.com");
            user.setPassword(null); // Explicitly set nullable fields if necessary
            user.setFullName(null);
            user.setDateOfBirth(null);
            user.setActive(false);
            user.setCreatedAt(null);
            user.setUpdatedAt(null);
            user.setClassObject(null);// Add this if `class_id` exists in your schema
    
            userDao.create(user);
            System.out.println("User created with ID: " + user.getId());
        } catch (Exception e) {
            System.err.println("Create operation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    private static void testReadOperation(Dao<User> userDao) {
        System.out.println("Testing Read Operation...");
        try {
            List<User> users = userDao.read(null); // Read all users
            System.out.println("Retrieved Users:");
            for (User user : users) {
                System.out.println("ID: " + user.getId() + ", Username: " + user.getUsername() + ", Email: " + user.getEmail());
            }
        } catch (Exception e) {
            System.err.println("Read operation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testUpdateOperation(Dao<User> userDao) throws SQLException, ReflectiveOperationException {
        System.out.println("Testing Update Operation...");
       
            Optional<User> user = userDao.findById(13);
            System.out.println(user.get().getFullName());
            user.get().setUsername("best");
            userDao.update(user.get(), "id=13");
            System.out.println("User updated successfully.");
    }

    private static void testDeleteOperation(Dao<User> userDao) {
        System.out.println("Testing Delete Operation...");
        try {
            List<User> users = userDao.read("username = 'john_doe'");
            if (!users.isEmpty()) {
                User user = users.get(0);
                userDao.delete("id = " + user.getId());
                System.out.println("User deleted successfully.");
            } else {
                System.out.println("No user found to delete.");
            }
        } catch (Exception e) {
            System.err.println("Delete operation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
