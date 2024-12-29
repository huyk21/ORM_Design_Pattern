package com.example;

import java.sql.SQLException;
import java.util.List;

import com.example.connection.DatabaseSession;
import com.example.connection.MySQLConnectionFactory;

public class Main {

    public static void main(String[] args) {
        // Create the MySQL connection factory
        MySQLConnectionFactory factory = MySQLConnectionFactory.createDefault(
                "localhost", "3306", "damframework", "root", "ducanh123"
        );

        DatabaseSession session = null;

        try {
            // Initialize the session
            session = new DatabaseSession(factory);
            System.out.println("Database connection established.");

            // Define your query
            String query = """
                SELECT c.id AS class_id, c.name AS class_name, 
                       u.id AS teacher_id, u.full_name AS teacher_name
                FROM classes c
                JOIN users u ON c.id = u.class_id
            """;

            // Execute the query
            List<Object[]> results = session.executeCustomJoinQuery(query);

            // Display the results
            for (Object[] row : results) {
                System.out.println("Class ID: " + row[0] + ", Class Name: " + row[1]);
                System.out.println("  Teacher ID: " + row[2] + ", Teacher Name: " + row[3]);
            }

        } catch (Exception e) {
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
