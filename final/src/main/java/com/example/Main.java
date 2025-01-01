// File: Main.java
package com.example;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.example.annotation.validator.NotNull;
import com.example.cache.CachedDao;
import com.example.cache.EvictionPolicy;
import com.example.cache.LRUEvictionPolicy;
import com.example.client.Class;
import com.example.client.Subject;
import com.example.client.User;
import com.example.connection.DatabaseSession;
import com.example.connection.MySQLConnectionFactory;
import com.example.connection.PostgreSQLConnectionFactory;
import com.example.connection.SqlServerConnectionFactory;
import com.example.entity.Dao;
import com.example.entity.GenericDaoImpl;
import com.example.schema.SchemaManager;
import com.example.schema.factory.MySQLStrategyFactory;
import com.example.validator.AlphanumericValidator;
import com.example.validator.ValidationProcessor;

/**
 * Entry point for testing the specific SELECT query using Generic DAO
 * implementation.
 */
public class Main {
    private static DatabaseSession session;

    public static void main(String[] args) {
        try {
            // Step 1: Create the connection factory with your database credentials
            MySQLConnectionFactory factory = MySQLConnectionFactory.createDefault(
                    "localhost", "3306", "ORMX", "root", "mysql");

//            PostgreSQLConnectionFactory factory = PostgreSQLConnectionFactory.createDefault(
//                    "localhost", "5432", "ORMX", "postgres", "postgres");
//
//            SqlServerConnectionFactory factory = SqlServerConnectionFactory.createDefault(
//                    "localhost", "1433", "ORMX", "sa", "SQLServer123@");

            // Step 2: Initialize the database session using the connection factory
            session = new DatabaseSession(factory);

            // Optional: Test database connection
            testDatabaseConnection(session);

            // Test Drop and Create Tables
            SchemaManager schemaManager = new SchemaManager(session, new MySQLStrategyFactory());
            testDropCreateTables(session, schemaManager);



            // Step 3: Initialize Generic DAO for User using the refactored GenericDaoImpl
            Dao<Class> classDao = new GenericDaoImpl<>(session, Class.class);
            Dao<User> userDao = new GenericDaoImpl<>(session, User.class);
            Dao<Subject> subjectDao = new GenericDaoImpl<>(session, Subject.class);

            // Test Create Operation
            testCreateOperation(userDao, subjectDao, classDao);


            // Step 4: Begin transaction (optional for SELECT operations, but included for
            // consistency)
            session.beginTransaction();

            testValidatorFunction();

            // Step 6: Commit transaction
            session.commitTransaction();
            System.out.println("Transaction committed successfully.");
            testSelectOperations(userDao);

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

                System.out.println(
                        "Username: " + username + ", Max User ID: " + maxUserId + ", Min User ID: " + minUserId);
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

    private static void testDropCreateTables(DatabaseSession session, SchemaManager schemaManager) throws SQLException {
        try {
            System.out.println("Testing Drop Tables...");
            schemaManager.dropTable(Subject.class);
            schemaManager.dropTable(User.class);
            schemaManager.dropTable(Class.class);
            System.out.println("Testing Create Tables...");
            schemaManager.createTable(Class.class);
            schemaManager.createTable(User.class);
            schemaManager.createTable(Subject.class);
        } catch (Exception e) {
            System.err.println("Drop/Create tables operation failed: " + e.getMessage());
            e.printStackTrace();
    }
    }

    private static void testCreateOperation(Dao<User> userDao, Dao<Subject> subjectDao, Dao<Class> classDao) {
        System.out.println("Testing Create Operation...");
        try {
            // Insert Classes
            Class class1A = new Class();
            class1A.setName("Class 1A");
            class1A.setYear(2023);
            classDao.create(class1A);

            Class class1B = new Class();
            class1B.setName("Class 1B");
            class1B.setYear(2023);
            classDao.create(class1B);

            Class class2A = new Class();
            class2A.setName("Class 2A");
            class2A.setYear(2024);
            classDao.create(class2A);

            // Insert Teachers
            User teacher1 = new User();
            teacher1.setUsername("teacher1");
            teacher1.setEmail("teacher1@example.com");
            teacher1.setPassword("password1");
            teacher1.setFullName("John Teacher");
            teacher1.setDateOfBirth(java.sql.Date.valueOf("1980-01-01"));
            teacher1.setActive(true);
            teacher1.setClassObject(class1A);
            userDao.create(teacher1);

            User teacher2 = new User();
            teacher2.setUsername("teacher2");
            teacher2.setEmail("teacher2@example.com");
            teacher2.setPassword("password2");
            teacher2.setFullName("Alice Teacher");
            teacher2.setDateOfBirth(java.sql.Date.valueOf("1985-02-02"));
            teacher2.setActive(true);
            teacher2.setClassObject(class2A);
            userDao.create(teacher2);

            // Insert Students
            User student1 = new User();
            student1.setUsername("student1");
            student1.setEmail("student1@example.com");
            student1.setPassword("password3");
            student1.setFullName("Jane Student");
            student1.setDateOfBirth(java.sql.Date.valueOf("2005-03-03"));
            student1.setActive(true);
            student1.setTeacher(teacher1);
            student1.setClassObject(class1A);
            userDao.create(student1);

            User student2 = new User();
            student2.setUsername("student2");
            student2.setEmail("student2@example.com");
            student2.setPassword("password4");
            student2.setFullName("Bob Student");
            student2.setDateOfBirth(java.sql.Date.valueOf("2006-04-04"));
            student2.setActive(true);
            student2.setTeacher(teacher1);
            student2.setClassObject(class1A);
            userDao.create(student2);

            User student3 = new User();
            student3.setUsername("student3");
            student3.setEmail("student3@example.com");
            student3.setPassword("password5");
            student3.setFullName("Eve Student");
            student3.setDateOfBirth(java.sql.Date.valueOf("2005-05-05"));
            student3.setActive(true);
            student3.setTeacher(teacher2);
            student3.setClassObject(class2A);
            userDao.create(student3);

            // Insert Subjects
            Subject subject1 = new Subject();
            subject1.setName("Mathematics");
            subject1.setCredit(3);
            subject1.setUser(teacher1);
            subjectDao.create(subject1);

            Subject subject2 = new Subject();
            subject2.setName("Physics");
            subject2.setCredit(4);
            subject2.setUser(teacher1);
            subjectDao.create(subject2);

            Subject subject3 = new Subject();
            subject3.setName("Chemistry");
            subject3.setCredit(3);
            subject3.setUser(teacher2);
            subjectDao.create(subject3);

            Subject subject4 = new Subject();
            subject4.setName("Biology");
            subject4.setCredit(3);
            subject4.setUser(teacher2);
            subjectDao.create(subject4);

            System.out.println("Data inserted successfully.");
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
                System.out.println(
                        "ID: " + user.getId() + ", Username: " + user.getUsername() + ", Email: " + user.getEmail());
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
