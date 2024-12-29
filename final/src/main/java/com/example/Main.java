package com.example;

import java.util.List;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.example.client.User;
import com.example.client.Classroom;
import com.example.connection.DatabaseSession;
import com.example.connection.MySQLConnectionFactory;

public class Main {

    public static void main(String[] args) {
        // Create the MySQL connection factory
        MySQLConnectionFactory factory = MySQLConnectionFactory.createDefault(
                "localhost", // host
                "3306", // port
                "dam_framework", // database
                "root", // username
                "mysql" // password
        );

        DatabaseSession session = null;

        try {
            // Create a DatabaseSession using the factory
            session = new DatabaseSession(factory);

            // Create a GenericDao for the User entity
            GenericDao<Classroom> dao = new GenericDao<>(session, Classroom.class);

            List<Classroom> classes = dao.select().join("root", "students", "s").get();
            for (var classroom : classes) {
                System.out.println(classroom.getName());
                for (var student : classroom.getStudents()) {
                    System.out.println("\t" + student.getFullName());
                }
            }
            System.out.println("------");

            // Start a transaction
            session.beginTransaction();

            GenericDao<User> userDao = new GenericDao<>(session, User.class);

            // Lazily load a user
            System.out.println("Attempting to lazily load user with ID 122...");
            User lazyUser = userDao.getLazy(User.class, 122);

            if (lazyUser != null) {
                // Access properties to trigger lazy loading
                System.out.println("User Full Name: " + lazyUser.getFullName());
            } else {
                System.out.println("User not found for ID 122.");
            }

            // Insert a new user for rollback testing
            User newUser = new User();
            newUser.setUsername("johndoe");
            newUser.setPassword("securepassword");
            newUser.setFullName("John Doe");
            newUser.setDateOfBirth(Date.valueOf("1990-01-01"));
            newUser.setActive(true);
            newUser.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            newUser.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            System.out.println("Saving new user...");
            userDao.create(newUser);
            System.out.println("User saved successfully.");

            

            // Commit transaction
            session.commitTransaction();
            System.out.println("Transaction committed successfully.");

        } catch (Exception e) {
            // Rollback transaction if an error occurs
            if (session != null) {
                try {
                    session.rollbackTransaction();
                    System.out.println("Transaction rolled back successfully.");
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            System.err.println("Operation failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure the session is closed
            if (session != null) {
                try {
                    session.closeConnection();
                    System.out.println("Database connection closed.");
                } catch (SQLException e) {
                    System.err.println("Failed to close database connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
